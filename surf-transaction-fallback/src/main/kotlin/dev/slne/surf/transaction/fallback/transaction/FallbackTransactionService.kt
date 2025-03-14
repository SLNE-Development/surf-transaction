package dev.slne.surf.transaction.fallback.transaction

import com.google.auto.service.AutoService
import dev.slne.surf.surfapi.core.api.util.objectSetOf
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.Transaction
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.TransactionResultType
import dev.slne.surf.transaction.api.user.TransactionUser
import dev.slne.surf.transaction.core.currency.currencyService
import dev.slne.surf.transaction.core.transaction.CoreTransaction
import dev.slne.surf.transaction.core.transaction.TransactionService
import dev.slne.surf.transaction.fallback.currency.FallbackCurrencyService
import dev.slne.surf.transaction.fallback.currency.FallbackCurrencyTable
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
        FallbackTransactionTable
            .innerJoin(FallbackCurrencyTable)
            .select(FallbackTransactionTable.amount.sum())
            .where {
                (FallbackCurrencyTable.name eq currency.name) and
                        (FallbackTransactionTable.receiver eq user)
            }
            .map { it[FallbackTransactionTable.amount.sum()] }
            .singleOrNull() ?: BigDecimal.ZERO
    }


    override suspend fun persistTransaction(transaction: Transaction): TransactionResultType =
        newSuspendedTransaction(Dispatchers.IO) {
            val fallbackCurrencyService = currencyService as FallbackCurrencyService
            val fallbackCurrency =
                fallbackCurrencyService.fallbackCurrencies.find { it.name == transaction.currency.name }!!

            val transactionSender = transaction.sender
            val transactionReceiver = transaction.receiver

            FallbackTransaction.new {
                identifier = transaction.identifier
                sender = transactionSender
                receiver = transactionReceiver
                amount = transaction.amount
                currency = fallbackCurrency
                extra = (transaction as CoreTransaction).data
            }

            if (transactionSender != null) {
                val balanceAfterTransaction =
                    balanceDecimal(transactionSender, transaction.currency)

                if (balanceAfterTransaction < BigDecimal.ZERO) {
                    rollback()

                    return@newSuspendedTransaction TransactionResult.SENDER_INSUFFICIENT_FUNDS to null
                }
            }

            if (transactionReceiver != null) {
                val balanceAfterTransaction =
                    balanceDecimal(transactionReceiver, transaction.currency)

                if (balanceAfterTransaction < BigDecimal.ZERO) {
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
        val senderResult = persistTransaction(senderTransaction)
        val receiverResult = persistTransaction(receiverTransaction)

        if (senderResult.first != TransactionResult.SUCCESS) {
            rollback()

            return@newSuspendedTransaction senderResult
        }

        if (receiverResult.first != TransactionResult.SUCCESS) {
            rollback()

            return@newSuspendedTransaction receiverResult
        }

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