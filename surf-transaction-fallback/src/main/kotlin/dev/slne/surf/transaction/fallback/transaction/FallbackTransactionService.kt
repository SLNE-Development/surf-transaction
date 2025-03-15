package dev.slne.surf.transaction.fallback.transaction

import com.google.auto.service.AutoService
import dev.slne.surf.surfapi.core.api.util.objectSetOf
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.Transaction
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.TransactionResultType
import dev.slne.surf.transaction.api.user.TransactionUser
import dev.slne.surf.transaction.core.currency.currencyService
import dev.slne.surf.transaction.core.transaction.TransactionService
import dev.slne.surf.transaction.fallback.currency.FallbackCurrencyService
import dev.slne.surf.transaction.fallback.currency.FallbackCurrencyTable
import dev.slne.surf.transaction.fallback.transaction.data.FallbackTransactionData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kyori.adventure.util.Services.Fallback
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.sum
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.math.BigDecimal
import java.util.*

@AutoService(TransactionService::class)
class FallbackTransactionService : TransactionService, Fallback {

    override suspend fun balanceDecimal(
        user: TransactionUser,
        currency: Currency
    ): BigDecimal = newSuspendedTransaction(Dispatchers.IO) {
        val result = FallbackTransactionTable
            .innerJoin(FallbackCurrencyTable)
            .select(FallbackTransactionTable.amount.sum())
            .where {
                (FallbackCurrencyTable.name eq currency.name) and
                        (FallbackTransactionTable.receiver eq user)
            }
            .map { it[FallbackTransactionTable.amount.sum()] }
            .singleOrNull()

        println("Raw DB result for balanceDecimal: $result")

        result ?: BigDecimal.ZERO
    }


    override suspend fun persistTransaction(transaction: Transaction): TransactionResultType =
        newSuspendedTransaction(Dispatchers.IO) {
            val fallbackCurrencyService = currencyService as FallbackCurrencyService
            val fallbackCurrency =
                fallbackCurrencyService.fallbackCurrencies.find { it.name == transaction.currency.name }!!

            val transactionSender = transaction.sender
            val transactionReceiver = transaction.receiver

            val fallbackTransaction = FallbackTransaction.new {
                identifier = transaction.identifier
                sender = transactionSender
                receiver = transactionReceiver
                amount = transaction.amount
                currency = fallbackCurrency
            }

            transaction.data.forEach {
                FallbackTransactionData.new {
                    this.transaction = fallbackTransaction.id

                    key = it.key
                    value = it.value
                }
            }

            if (transactionSender != null && transaction.amount < BigDecimal.ZERO) {
                val balanceAfterTransaction =
                    balanceDecimal(transactionSender, transaction.currency)

                println("Balance after transaction sender: $balanceAfterTransaction")

                if (
                    balanceAfterTransaction < transaction.currency.minimumAmount &&
                    !transaction.ignoreMinimumAmount
                ) {
                    rollback()

                    return@newSuspendedTransaction TransactionResult.SENDER_INSUFFICIENT_FUNDS to null
                }
            }

            if (transactionReceiver != null && transaction.amount < BigDecimal.ZERO) {
                val balanceAfterTransaction =
                    balanceDecimal(transactionReceiver, transaction.currency)

                println("Balance after transaction receiver: $balanceAfterTransaction")

                if (
                    balanceAfterTransaction < transaction.currency.minimumAmount &&
                    !transaction.ignoreMinimumAmount
                ) {
                    rollback()

                    return@newSuspendedTransaction TransactionResult.RECEIVER_INSUFFICIENT_FUNDS to null
                }
            }

            TransactionResult.SUCCESS to transaction
        }

    override suspend fun transfer(
        senderTransaction: Transaction,
        receiverTransaction: Transaction
    ): TransactionResultType = newSuspendedTransaction(Dispatchers.IO) {
        println("Sender transaction: $senderTransaction")
        println("Receiver transaction: $receiverTransaction")
        println("#".repeat(30))

        println("Executing sender transaction")
        val senderResult = persistTransaction(senderTransaction)
        if (senderResult.first != TransactionResult.SUCCESS) {
            println("Sender result: $senderResult")
            rollback()

            return@newSuspendedTransaction senderResult
        }
        println("-".repeat(30))

        println("Executing receiver transaction")
        val receiverResult = persistTransaction(receiverTransaction)
        if (receiverResult.first != TransactionResult.SUCCESS) {
            println("Receiver result: $receiverResult")
            rollback()

            return@newSuspendedTransaction receiverResult
        }
        println("-".repeat(30))

        TransactionResult.SUCCESS to senderTransaction
    }

    override suspend fun generateTransactionId(vararg excludingIds: UUID): UUID =
        withContext(Dispatchers.IO) {
            val excludingIdSet = objectSetOf(*excludingIds)
            var generatedId = UUID.randomUUID()

            while (existsTransactionById(generatedId) || generatedId in excludingIdSet) {
                generatedId = UUID.randomUUID()
            }

            generatedId
        }

    private suspend fun existsTransactionById(identifier: UUID) =
        newSuspendedTransaction(Dispatchers.IO) {
            !FallbackTransaction.find {
                FallbackTransactionTable.identifier eq identifier
            }.empty()
        }
}