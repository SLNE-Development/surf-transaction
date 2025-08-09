package dev.slne.surf.transaction.server.transaction

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.core.transaction.TransactionImpl
import it.unimi.dsi.fastutil.objects.ObjectSet
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

@Service
class TransactionService(private val transactionRepository: TransactionRepository) {

    suspend fun deposit(
        initiator: OfflineCloudPlayer?,
        accountId: UUID,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult {
        val transaction = TransactionImpl(
            identifier = UUID.randomUUID(),
            initiator = initiator,
            senderAccountId = null,
            receiverAccountId = accountId,
            amount = amount,
            currencyName = currency.name,
            ignoreMinimumAmount = ignoreMinimum,
            data = additionalData.toSet()
        )

        return transactionRepository.persistTransaction(transaction)
    }

    suspend fun withdraw(
        initiator: OfflineCloudPlayer?,
        accountId: UUID,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult {
        val usableAmount = amount.abs().negate()

        val transaction = TransactionImpl(
            identifier = UUID.randomUUID(),
            senderAccountId = null,
            initiator = initiator,
            receiverAccountId = accountId,
            amount = usableAmount,
            currencyName = currency.name,
            ignoreMinimumAmount = ignoreMinimum,
            data = additionalData.toSet()
        )

        return transactionRepository.persistTransaction(transaction)
    }

    suspend fun transfer(
        initiator: OfflineCloudPlayer?,
        senderAccountId: UUID,
        amount: BigDecimal,
        currency: Currency,
        receiverAccountId: UUID,
        ignoreSenderMinimum: Boolean,
        ignoreReceiverMinimum: Boolean,
        additionalSenderData: ObjectSet<TransactionData>,
        additionalReceiverData: ObjectSet<TransactionData>
    ): TransactionResult {
        val senderAmount = amount.abs().negate()
        val receiverAmount = amount.abs()

        val senderTransaction = TransactionImpl(
            initiator = initiator,
            identifier = UUID.randomUUID(),
            senderAccountId = receiverAccountId,
            receiverAccountId = senderAccountId,
            amount = senderAmount,
            currencyName = currency.name,
            ignoreMinimumAmount = ignoreSenderMinimum,
            data = additionalSenderData.toSet()
        )

        val receiverTransaction = TransactionImpl(
            initiator = initiator,
            identifier = UUID.randomUUID(),
            senderAccountId = senderAccountId,
            receiverAccountId = receiverAccountId,
            amount = receiverAmount,
            currencyName = currency.name,
            ignoreMinimumAmount = ignoreReceiverMinimum,
            data = additionalReceiverData.toSet()
        )

        return transactionRepository.transfer(senderTransaction, receiverTransaction)
    }
    
    suspend fun balanceDecimal(
        account: Account,
        currency: Currency
    ): BigDecimal = transactionRepository.balanceDecimal(account.accountId, currency)

    suspend fun balanceDecimal(
        accountId: UUID,
        currency: Currency,
    ): BigDecimal = transactionRepository.balanceDecimal(accountId, currency)
}