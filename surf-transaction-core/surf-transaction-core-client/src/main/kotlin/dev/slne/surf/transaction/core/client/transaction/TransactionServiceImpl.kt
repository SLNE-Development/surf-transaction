package dev.slne.surf.transaction.core.client.transaction

import com.google.auto.service.AutoService
import dev.slne.surf.rabbitmq.api.exception.SurfRabbitRequestTimeoutException
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.PendingReservationTimeoutException
import dev.slne.surf.transaction.api.transaction.PendingTransactionResult
import dev.slne.surf.transaction.api.transaction.Transaction
import dev.slne.surf.transaction.api.transaction.TransactionCommitResult
import dev.slne.surf.transaction.api.transaction.TransactionRollbackResult
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.TransactionService
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.core.client.rabbitApi
import dev.slne.surf.transaction.core.common.protocol.transaction.balance.GetTransactionBalanceRequestPacket
import dev.slne.surf.transaction.core.common.protocol.transaction.create.CreateTransactionRequestPacket
import dev.slne.surf.transaction.core.common.protocol.transaction.transfer.TransferTransactionCreateRequestPacket
import dev.slne.surf.transaction.core.common.rpc.TransactionRpcService
import dev.slne.surf.transaction.core.common.transaction.CoreTransactionService
import dev.slne.surf.transaction.core.common.transaction.TransactionImpl
import java.math.BigDecimal
import java.util.*
import kotlin.time.Duration

@AutoService(TransactionService::class)
class TransactionServiceImpl : CoreTransactionService {
    private val transactionRpc by lazy {
        rabbitApi.createRpcService<TransactionRpcService>()
    }

    override suspend fun beginDeposit(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): PendingTransactionResult {
        val timeoutMillis = timeout.timeoutMillisOrNull()
            ?: return PendingTransactionResult.InvalidTimeout
        val transaction = createTransaction(
            account = account,
            initiator = initiator,
            amount = amount.abs(),
            currency = currency,
            ignoreMinimum = ignoreMinimum,
            additionalData = additionalData.toSet()
        )

        return pendingRpcRequest(setOf(transaction.identifier)) {
            transactionRpc.beginTransaction(transaction, timeoutMillis)
        }
    }

    override suspend fun beginWithdrawal(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): PendingTransactionResult {
        val timeoutMillis = timeout.timeoutMillisOrNull()
            ?: return PendingTransactionResult.InvalidTimeout
        val transaction = createTransaction(
            account = account,
            initiator = initiator,
            amount = amount.abs().negate(),
            currency = currency,
            ignoreMinimum = ignoreMinimum,
            additionalData = additionalData.toSet()
        )

        return pendingRpcRequest(setOf(transaction.identifier)) {
            transactionRpc.beginTransaction(transaction, timeoutMillis)
        }
    }

    override suspend fun beginTransfer(
        initiator: UUID,
        sender: Account,
        amount: BigDecimal,
        currency: Currency,
        receiver: Account,
        timeout: Duration,
        ignoreSenderMinimum: Boolean,
        ignoreReceiverMinimum: Boolean,
        additionalSenderData: Set<TransactionData>,
        additionalReceiverData: Set<TransactionData>
    ): PendingTransactionResult {
        val timeoutMillis = timeout.timeoutMillisOrNull()
            ?: return PendingTransactionResult.InvalidTimeout
        val (senderTransaction, receiverTransaction) = createTransferTransactions(
            initiator,
            sender,
            amount,
            currency,
            receiver,
            ignoreSenderMinimum,
            ignoreReceiverMinimum,
            additionalSenderData,
            additionalReceiverData
        )

        return pendingRpcRequest(
            setOf(senderTransaction.identifier, receiverTransaction.identifier)
        ) {
            transactionRpc.beginTransfer(
                senderTransaction,
                receiverTransaction,
                timeoutMillis
            )
        }
    }

    override suspend fun deposit(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult {
        val transaction = createTransaction(
            account = account,
            initiator = initiator,
            amount = amount.abs(),
            currency = currency,
            ignoreMinimum = ignoreMinimum,
            additionalData = additionalData.toSet()
        )

        val (result) = rabbitApi.sendRequest(CreateTransactionRequestPacket(transaction))
        return result
    }

    override suspend fun withdraw(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult {
        val transaction = createTransaction(
            account = account,
            initiator = initiator,
            amount = amount.abs().negate(),
            currency = currency,
            ignoreMinimum = ignoreMinimum,
            additionalData = additionalData.toSet()
        )

        val (result) = rabbitApi.sendRequest(CreateTransactionRequestPacket(transaction))
        return result
    }

    override suspend fun transfer(
        initiator: UUID,
        sender: Account,
        amount: BigDecimal,
        currency: Currency,
        receiver: Account,
        ignoreSenderMinimum: Boolean,
        ignoreReceiverMinimum: Boolean,
        additionalSenderData: Set<TransactionData>,
        additionalReceiverData: Set<TransactionData>
    ): TransactionResult {
        val (senderTransaction, receiverTransaction) = createTransferTransactions(
            initiator,
            sender,
            amount,
            currency,
            receiver,
            ignoreSenderMinimum,
            ignoreReceiverMinimum,
            additionalSenderData,
            additionalReceiverData
        )

        val (result) = rabbitApi.sendRequest(
            TransferTransactionCreateRequestPacket(senderTransaction, receiverTransaction)
        )
        return result
    }

    override suspend fun balance(
        account: Account,
        currency: Currency
    ): BigDecimal {
        val request = GetTransactionBalanceRequestPacket(account.accountId, currency.name)
        val (balance) = rabbitApi.sendRequest(request)

        return balance
    }

    override suspend fun commit(identifier: UUID): TransactionCommitResult = transactionRpc.commit(identifier)
    override suspend fun rollback(identifier: UUID): TransactionRollbackResult = transactionRpc.rollback(identifier)
    override suspend fun find(identifier: UUID): Transaction? = transactionRpc.find(identifier)

    private fun createTransaction(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        additionalData: Set<TransactionData>
    ) = TransactionImpl(
        identifier = UUID.randomUUID(),
        senderAccountId = null,
        initiator = initiator,
        receiverAccountId = account.accountId,
        amount = amount,
        currencyName = currency.name,
        ignoreMinimumAmount = ignoreMinimum,
        data = additionalData
    )

    private fun createTransferTransactions(
        initiator: UUID,
        sender: Account,
        amount: BigDecimal,
        currency: Currency,
        receiver: Account,
        ignoreSenderMinimum: Boolean,
        ignoreReceiverMinimum: Boolean,
        additionalSenderData: Set<TransactionData>,
        additionalReceiverData: Set<TransactionData>
    ): Pair<TransactionImpl, TransactionImpl> {
        val senderTransaction = TransactionImpl(
            initiator = initiator,
            identifier = UUID.randomUUID(),
            senderAccountId = receiver.accountId,
            receiverAccountId = sender.accountId,
            amount = amount.abs().negate(),
            currencyName = currency.name,
            ignoreMinimumAmount = ignoreSenderMinimum,
            data = additionalSenderData.toSet()
        )
        val receiverTransaction = TransactionImpl(
            initiator = initiator,
            identifier = UUID.randomUUID(),
            senderAccountId = sender.accountId,
            receiverAccountId = receiver.accountId,
            amount = amount.abs(),
            currencyName = currency.name,
            ignoreMinimumAmount = ignoreReceiverMinimum,
            data = additionalReceiverData.toSet()
        )
        return senderTransaction to receiverTransaction
    }

    private fun Duration.timeoutMillisOrNull(): Long? =
        takeIf { it.isFinite() && it.isPositive() }
            ?.inWholeMilliseconds
            ?.takeIf { it > 0 }

    private suspend fun <T> pendingRpcRequest(
        transactionIdentifiers: Set<UUID>,
        request: suspend () -> T
    ): T = try {
        request()
    } catch (cause: SurfRabbitRequestTimeoutException) {
        throw PendingReservationTimeoutException(transactionIdentifiers, cause)
    }

    companion object {
        val INSTANCE get() = TransactionService.INSTANCE as TransactionServiceImpl
    }
}
