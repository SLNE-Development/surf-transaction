package dev.slne.surf.transaction.core.user

import dev.slne.surf.surfapi.core.api.util.toMutableObjectSet
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResultType
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.user.TransactionUser
import dev.slne.surf.transaction.core.transaction.CoreTransaction
import dev.slne.surf.transaction.core.transaction.TransactionService
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
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResultType {
        val transactionId = TransactionService.generateTransactionId()

        val transaction = CoreTransaction(
            identifier = transactionId,
            sender = null,
            receiver = this,
            amount = amount,
            currency = currency,
            ignoreMinimumAmount = ignoreMinimum,
            data = additionalData.toMutableObjectSet()
        )

        return TransactionService.persistTransaction(transaction)
    }

    override suspend fun withdraw(
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResultType {
        val transactionId = TransactionService.generateTransactionId()
        val usableAmount = amount.abs().negate()

        val transaction = CoreTransaction(
            identifier = transactionId,
            sender = null,
            receiver = this,
            amount = usableAmount,
            currency = currency,
            ignoreMinimumAmount = ignoreMinimum,
            data = additionalData.toMutableObjectSet()
        )

        return TransactionService.persistTransaction(transaction)
    }

    override suspend fun transfer(
        amount: BigDecimal,
        currency: Currency,
        receiver: TransactionUser,
        ignoreSenderMinimum: Boolean,
        ignoreReceiverMinimum: Boolean,
        additionalSenderData: ObjectSet<TransactionData>,
        additionalReceiverData: ObjectSet<TransactionData>
    ): TransactionResultType {
        val senderTransactionId = TransactionService.generateTransactionId()
        val receiverTransactionId = TransactionService.generateTransactionId()

        val senderAmount = amount.abs().negate()
        val receiverAmount = amount.abs()

        val senderTransaction = CoreTransaction(
            identifier = senderTransactionId,
            sender = receiver,
            receiver = this,
            amount = senderAmount,
            currency = currency,
            ignoreMinimumAmount = ignoreSenderMinimum,
            data = additionalSenderData.toMutableObjectSet()
        )

        val receiverTransaction = CoreTransaction(
            identifier = receiverTransactionId,
            sender = this,
            receiver = receiver,
            amount = receiverAmount,
            currency = currency,
            ignoreMinimumAmount = ignoreReceiverMinimum,
            data = additionalReceiverData.toMutableObjectSet()
        )

        return TransactionService.transfer(senderTransaction, receiverTransaction)
    }

    override suspend fun balanceDecimal(currency: Currency) =
        TransactionService.balanceDecimal(this, currency)

    override fun toString(): String {
        return "CoreTransactionUser(uuid=$uuid)"
    }
}