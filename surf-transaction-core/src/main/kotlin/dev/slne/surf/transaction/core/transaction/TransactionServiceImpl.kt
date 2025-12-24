package dev.slne.surf.transaction.core.transaction

import com.google.auto.service.AutoService
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.TransactionService
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.core.db.transaction.TransactionRepository
import java.math.BigDecimal
import java.util.UUID

@AutoService(TransactionService::class)
class TransactionServiceImpl: TransactionService {
    override suspend fun deposit(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult {
        val transaction = TransactionImpl(
            identifier = UUID.randomUUID(),
            initiator = initiator,
            senderAccountId = null,
            receiverAccountId = account.accountId,
            amount = amount,
            currencyName = currency.name,
            ignoreMinimumAmount = ignoreMinimum,
            data = additionalData.toSet()
        )

        return TransactionRepository.persistTransaction(transaction)
    }

    override suspend fun withdraw(
        account: Account,
        initiator: UUID,
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
            receiverAccountId = account.accountId,
            amount = usableAmount,
            currencyName = currency.name,
            ignoreMinimumAmount = ignoreMinimum,
            data = additionalData.toSet()
        )

        return TransactionRepository.persistTransaction(transaction)
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
        val senderAmount = amount.abs().negate()
        val receiverAmount = amount.abs()

        val senderTransaction = TransactionImpl(
            initiator = initiator,
            identifier = UUID.randomUUID(),
            senderAccountId = receiver.accountId,
            receiverAccountId = sender.accountId,
            amount = senderAmount,
            currencyName = currency.name,
            ignoreMinimumAmount = ignoreSenderMinimum,
            data = additionalSenderData.toSet()
        )

        val receiverTransaction = TransactionImpl(
            initiator = initiator,
            identifier = UUID.randomUUID(),
            senderAccountId = sender.accountId,
            receiverAccountId = receiver.accountId,
            amount = receiverAmount,
            currencyName = currency.name,
            ignoreMinimumAmount = ignoreReceiverMinimum,
            data = additionalReceiverData.toSet()
        )

        return TransactionRepository.transfer(senderTransaction, receiverTransaction)
    }

    override suspend fun balance(
        account: Account,
        currency: Currency
    ): BigDecimal {
        return TransactionRepository.balanceDecimal(account.accountId, currency)
    }

    companion object {
        fun get() = TransactionService.instance as TransactionServiceImpl
    }
}