package dev.slne.surf.transaction.core.client.transaction

import com.google.auto.service.AutoService
import dev.slne.surf.microservice.api.rabbit.client.ClientRabbitMQApi
import dev.slne.surf.surfapi.core.api.util.requiredService
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.TransactionService
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.core.rabbit.transaction.BalanceRequest
import dev.slne.surf.transaction.core.rabbit.transaction.DepositRequest
import dev.slne.surf.transaction.core.rabbit.transaction.TransactionResultPacket
import dev.slne.surf.transaction.core.rabbit.transaction.TransferRequest
import dev.slne.surf.transaction.core.rabbit.transaction.WithdrawRequest
import dev.slne.surf.transaction.core.transaction.CoreTransactionService
import java.math.BigDecimal
import java.util.UUID

@AutoService(TransactionService::class)
class TransactionServiceClientImpl : CoreTransactionService {
    private val rabbitApi get() = requiredService<ClientRabbitMQApi>()

    override suspend fun deposit(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult {
        val response = rabbitApi.sendRequest(
            DepositRequest(
                accountId = account.accountId,
                initiator = initiator,
                amount = amount,
                currencyName = currency.name,
                ignoreMinimum = ignoreMinimum,
                additionalData = additionalData.toSet()
            )
        )
        return response.result.toTransactionResult()
    }

    override suspend fun withdraw(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult {
        val response = rabbitApi.sendRequest(
            WithdrawRequest(
                accountId = account.accountId,
                initiator = initiator,
                amount = amount,
                currencyName = currency.name,
                ignoreMinimum = ignoreMinimum,
                additionalData = additionalData.toSet()
            )
        )
        return response.result.toTransactionResult()
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
        val response = rabbitApi.sendRequest(
            TransferRequest(
                initiator = initiator,
                senderAccountId = sender.accountId,
                amount = amount,
                currencyName = currency.name,
                receiverAccountId = receiver.accountId,
                ignoreSenderMinimum = ignoreSenderMinimum,
                ignoreReceiverMinimum = ignoreReceiverMinimum,
                additionalSenderData = additionalSenderData,
                additionalReceiverData = additionalReceiverData
            )
        )
        return response.result.toTransactionResult()
    }

    override suspend fun balance(account: Account, currency: Currency): BigDecimal {
        val response = rabbitApi.sendRequest(
            BalanceRequest(accountId = account.accountId, currencyName = currency.name)
        )
        return response.balance
    }

    private fun TransactionResultPacket.toTransactionResult(): TransactionResult = when (this) {
        is TransactionResultPacket.Success -> TransactionResult.Success(transaction)
        is TransactionResultPacket.TransferSuccess ->
            TransactionResult.TransferSuccess(senderTransaction, receiverTransaction)
        TransactionResultPacket.ReceiverInsufficientFunds ->
            TransactionResult.ReceiverInsufficientFunds
        TransactionResultPacket.SenderInsufficientFunds ->
            TransactionResult.SenderInsufficientFunds
        is TransactionResultPacket.DatabaseError ->
            TransactionResult.DatabaseError(RuntimeException(message))
    }
}
