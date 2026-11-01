package dev.slne.surf.transaction.core.client.transaction

import com.google.auto.service.AutoService
import dev.slne.surf.rabbitmq.api.exception.SurfRabbitRequestTimeoutException
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.currency.CurrencyDisabledException
import dev.slne.surf.transaction.api.transaction.PendingReservationTimeoutException
import dev.slne.surf.transaction.api.transaction.PendingTransactionResult
import dev.slne.surf.transaction.api.transaction.Transaction
import dev.slne.surf.transaction.api.transaction.TransactionCommitResult
import dev.slne.surf.transaction.api.transaction.TransactionRollbackResult
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.TransactionService
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.core.client.balance.BalanceCache
import dev.slne.surf.transaction.core.client.currency.CurrencyServiceImpl
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
        vararg additionalData: TransactionData,
        ignoreDisabled: Boolean
    ): PendingTransactionResult {
        if (!ignoreDisabled && isDisabled(currency)) {
            return PendingTransactionResult.CurrencyDisabled(currency.name)
        }
        val timeoutMillis = timeout.timeoutMillisOrNull()
            ?: return PendingTransactionResult.InvalidTimeout
        val transaction = createTransaction(
            account = account,
            initiator = initiator,
            amount = amount.abs(),
            currency = currency,
            ignoreMinimum = ignoreMinimum,
            additionalData = additionalData.toSet(),
            ignoreDisabled = ignoreDisabled
        )

        return pendingRpcRequest(setOf(transaction.identifier)) {
            transactionRpc.beginTransaction(transaction, timeoutMillis)
        }.also { BalanceCache.balancesChanged(it.transactions) }
    }

    override suspend fun beginWithdrawal(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData,
        ignoreDisabled: Boolean
    ): PendingTransactionResult {
        if (!ignoreDisabled && isDisabled(currency)) {
            return PendingTransactionResult.CurrencyDisabled(currency.name)
        }
        val timeoutMillis = timeout.timeoutMillisOrNull()
            ?: return PendingTransactionResult.InvalidTimeout
        val transaction = createTransaction(
            account = account,
            initiator = initiator,
            amount = amount.abs().negate(),
            currency = currency,
            ignoreMinimum = ignoreMinimum,
            additionalData = additionalData.toSet(),
            ignoreDisabled = ignoreDisabled
        )

        return pendingRpcRequest(setOf(transaction.identifier)) {
            transactionRpc.beginTransaction(transaction, timeoutMillis)
        }.also { BalanceCache.balancesChanged(it.transactions) }
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
        additionalReceiverData: Set<TransactionData>,
        ignoreDisabled: Boolean
    ): PendingTransactionResult {
        if (!ignoreDisabled && isDisabled(currency)) {
            return PendingTransactionResult.CurrencyDisabled(currency.name)
        }
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
            additionalReceiverData,
            ignoreDisabled
        )

        return pendingRpcRequest(
            setOf(senderTransaction.identifier, receiverTransaction.identifier)
        ) {
            transactionRpc.beginTransfer(
                senderTransaction,
                receiverTransaction,
                timeoutMillis
            )
        }.also { BalanceCache.balancesChanged(it.transactions) }
    }

    override suspend fun deposit(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData,
        ignoreDisabled: Boolean
    ): TransactionResult {
        if (!ignoreDisabled && isDisabled(currency)) {
            return TransactionResult.CurrencyDisabled(currency.name)
        }
        val transaction = createTransaction(
            account = account,
            initiator = initiator,
            amount = amount.abs(),
            currency = currency,
            ignoreMinimum = ignoreMinimum,
            additionalData = additionalData.toSet(),
            ignoreDisabled = ignoreDisabled
        )

        val (result) = rabbitApi.sendRequest(CreateTransactionRequestPacket(transaction))
        BalanceCache.balancesChanged(result.transactions)
        return result
    }

    override suspend fun withdraw(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData,
        ignoreDisabled: Boolean
    ): TransactionResult {
        if (!ignoreDisabled && isDisabled(currency)) {
            return TransactionResult.CurrencyDisabled(currency.name)
        }
        val transaction = createTransaction(
            account = account,
            initiator = initiator,
            amount = amount.abs().negate(),
            currency = currency,
            ignoreMinimum = ignoreMinimum,
            additionalData = additionalData.toSet(),
            ignoreDisabled = ignoreDisabled
        )

        val (result) = rabbitApi.sendRequest(CreateTransactionRequestPacket(transaction))
        BalanceCache.balancesChanged(result.transactions)
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
        additionalReceiverData: Set<TransactionData>,
        ignoreDisabled: Boolean
    ): TransactionResult {
        if (!ignoreDisabled && isDisabled(currency)) {
            return TransactionResult.CurrencyDisabled(currency.name)
        }
        val (senderTransaction, receiverTransaction) = createTransferTransactions(
            initiator,
            sender,
            amount,
            currency,
            receiver,
            ignoreSenderMinimum,
            ignoreReceiverMinimum,
            additionalSenderData,
            additionalReceiverData,
            ignoreDisabled
        )

        val (result) = rabbitApi.sendRequest(
            TransferTransactionCreateRequestPacket(senderTransaction, receiverTransaction)
        )
        BalanceCache.balancesChanged(result.transactions)
        return result
    }

    override suspend fun balance(
        account: Account,
        currency: Currency,
        ignoreDisabled: Boolean
    ): BigDecimal {
        if (!ignoreDisabled && isDisabled(currency)) throw CurrencyDisabledException(currency.name)

        val request =
            GetTransactionBalanceRequestPacket(account.accountId, currency.name, ignoreDisabled)
        val (balance, currencyDisabled) = rabbitApi.sendRequest(request)
        if (currencyDisabled) throw CurrencyDisabledException(currency.name)

        return balance
    }

    override suspend fun commit(identifier: UUID): TransactionCommitResult =
        transactionRpc.commit(identifier).also { BalanceCache.balancesChanged(it.transactions) }

    override suspend fun rollback(identifier: UUID): TransactionRollbackResult =
        transactionRpc.rollback(identifier).also { BalanceCache.balancesChanged(it.transactions) }
    override suspend fun find(identifier: UUID): Transaction? = transactionRpc.find(identifier)

    private fun createTransaction(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        additionalData: Set<TransactionData>,
        ignoreDisabled: Boolean
    ) = TransactionImpl(
        identifier = UUID.randomUUID(),
        senderAccountId = null,
        initiator = initiator,
        receiverAccountId = account.accountId,
        amount = amount,
        currencyName = currency.name,
        ignoreMinimumAmount = ignoreMinimum,
        data = additionalData,
        ignoreDisabledCurrency = ignoreDisabled
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
        additionalReceiverData: Set<TransactionData>,
        ignoreDisabled: Boolean
    ): Pair<TransactionImpl, TransactionImpl> {
        val senderTransaction = TransactionImpl(
            initiator = initiator,
            identifier = UUID.randomUUID(),
            senderAccountId = receiver.accountId,
            receiverAccountId = sender.accountId,
            amount = amount.abs().negate(),
            currencyName = currency.name,
            ignoreMinimumAmount = ignoreSenderMinimum,
            data = additionalSenderData.toSet(),
            ignoreDisabledCurrency = ignoreDisabled
        )
        val receiverTransaction = TransactionImpl(
            initiator = initiator,
            identifier = UUID.randomUUID(),
            senderAccountId = sender.accountId,
            receiverAccountId = receiver.accountId,
            amount = amount.abs(),
            currencyName = currency.name,
            ignoreMinimumAmount = ignoreReceiverMinimum,
            data = additionalReceiverData.toSet(),
            ignoreDisabledCurrency = ignoreDisabled
        )
        return senderTransaction to receiverTransaction
    }

    /**
     * Checks the local registry, which may lag behind the microservice; the microservice enforces
     * the disabled state authoritatively.
     */
    private fun isDisabled(currency: Currency): Boolean =
        CurrencyServiceImpl.INSTANCE.getCurrencyByName(currency.name)?.enabled == false

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

/** Transactions whose accounts' balances changed through this result. */
private val TransactionResult.transactions: List<Transaction>
    get() = when (this) {
        is TransactionResult.Success -> listOf(transaction)
        is TransactionResult.TransferSuccess -> listOf(senderTransaction, receiverTransaction)
        else -> emptyList()
    }

/** Transactions whose accounts' available balances changed by reserving them. */
private val PendingTransactionResult.transactions: List<Transaction>
    get() = when (this) {
        is PendingTransactionResult.Created -> listOf(transaction)
        is PendingTransactionResult.TransferCreated -> listOf(senderTransaction, receiverTransaction)
        else -> emptyList()
    }

/** Transactions whose accounts' balances changed by committing or expiring them. */
private val TransactionCommitResult.transactions: List<Transaction>
    get() = when (this) {
        is TransactionCommitResult.Committed -> listOf(transaction)
        is TransactionCommitResult.Expired -> listOf(transaction)
        else -> emptyList()
    }

/** Transactions whose accounts' available balances changed by releasing their reservation. */
private val TransactionRollbackResult.transactions: List<Transaction>
    get() = when (this) {
        is TransactionRollbackResult.RolledBack -> listOf(transaction)
        is TransactionRollbackResult.Expired -> listOf(transaction)
        else -> emptyList()
    }
