package dev.slne.surf.transaction.core.user

import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResultType
import dev.slne.surf.transaction.api.user.TransactionUser
import dev.slne.surf.transaction.core.transaction.CoreTransaction
import dev.slne.surf.transaction.core.transaction.transactionService
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.math.BigDecimal
import java.util.*

/**
 * A core implementation of a [TransactionUser]
 */
class CoreTransactionUser(override val uuid: UUID) : TransactionUser {

    override suspend fun deposit(
        amount: BigDecimal,
        currency: Currency,
        vararg additionalData: Any
    ): TransactionResultType {
        val transactionId = transactionService.generateTransactionId()

        val transaction = CoreTransaction(
            identifier = transactionId,
            sender = null,
            receiver = this,
            amount = amount,
            currency = currency
        ).apply { additionalData.forEach { this.addData(it) } }

        return transactionService.persistTransaction(transaction)
    }

    override suspend fun withdraw(
        amount: BigDecimal,
        currency: Currency,
        vararg additionalData: Any
    ): TransactionResultType {
        val transactionId = transactionService.generateTransactionId()
        val usableAmount = amount.abs().negate()

        val transaction = CoreTransaction(
            identifier = transactionId,
            sender = this,
            receiver = null,
            amount = usableAmount,
            currency = currency
        ).apply { additionalData.forEach { this.addData(it) } }

        return transactionService.persistTransaction(transaction)
    }

    override suspend fun transfer(
        amount: BigDecimal,
        currency: Currency,
        receiver: TransactionUser,
        additionalSenderData: ObjectSet<Any>,
        additionalReceiverData: ObjectSet<Any>
    ): TransactionResultType {
        val senderTransactionId = transactionService.generateTransactionId()
        val receiverTransactionId = transactionService.generateTransactionId()

        val senderAmount = amount.abs().negate()
        val receiverAmount = amount.abs()

        val senderTransaction = CoreTransaction(
            identifier = senderTransactionId,
            sender = this,
            receiver = receiver,
            amount = senderAmount,
            currency = currency
        ).apply { additionalSenderData.forEach { this.addData(it) } }

        val receiverTransaction = CoreTransaction(
            identifier = receiverTransactionId,
            sender = this,
            receiver = receiver,
            amount = receiverAmount,
            currency = currency
        ).apply { additionalReceiverData.forEach { this.addData(it) } }

        return transactionService.transfer(senderTransaction, receiverTransaction)
    }

    override suspend fun balanceDecimal(currency: Currency) =
        transactionService.balanceDecimal(this, currency)
}