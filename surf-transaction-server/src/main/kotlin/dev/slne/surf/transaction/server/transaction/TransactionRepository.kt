package dev.slne.surf.transaction.server.transaction

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.server.plugin.CoroutineTransactional
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.Transaction
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.server.currency.CurrencyRepository
import dev.slne.surf.transaction.server.currency.db.CurrencyTable
import dev.slne.surf.transaction.server.transaction.db.TransactionDataTable
import dev.slne.surf.transaction.server.transaction.db.TransactionEntity
import dev.slne.surf.transaction.server.transaction.db.TransactionTable
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.sum
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.util.*

/**
 * A service to handle transactions
 */
@Repository
@CoroutineTransactional
class TransactionRepository(private val currencyRepository: CurrencyRepository) {

    /**
     * Get the balance of a user
     *
     * @param user The user to get the balance of
     * @param currency The currency to get the balance in
     *
     * @return The balance of the user
     */
    suspend fun balanceDecimal(user: OfflineCloudPlayer, currency: Currency, forUpdate: Boolean = false): BigDecimal =
        TransactionTable
            .innerJoin(CurrencyTable)
            .select(TransactionTable.amount.sum())
            .where {
                (CurrencyTable.name eq currency.name) and
                        (TransactionTable.receiver eq user.uuid)
            }
            .let { if (forUpdate) it.forUpdate() else it }
            .map { it[TransactionTable.amount.sum()] }
            .singleOrNull() ?: BigDecimal.ZERO

    /**
     * Execute a transaction
     *
     * @param transaction The transaction to execute
     *
     * @return The result of the transaction
     */
    suspend fun persistTransaction(transaction: Transaction): TransactionResult {
        val currency = currencyRepository.fetchCurrencyByName(transaction.currency.name)
            ?: error("Currency not found: ${transaction.currency.name}")
        val receiver = transaction.receiver

        val persistedTransaction = TransactionEntity.new {
            identifier = transaction.identifier
            sender = transaction.sender?.uuid
            this.receiver = receiver?.uuid
            amount = transaction.amount
            this.currency = currency
        }

        TransactionDataTable.batchInsert(transaction.data, shouldReturnGeneratedValues = false) {
            this[TransactionDataTable.transaction] = persistedTransaction.id
            this[TransactionDataTable.key] = it.key
            this[TransactionDataTable.value] = it.value
        }

        if (receiver != null && !transaction.ignoreMinimumAmount) {
            val balanceAfterTransaction = balanceDecimal(receiver, transaction.currency, forUpdate = true)
            if (balanceAfterTransaction < transaction.currency.minimumAmount) {
                TransactionManager.current().rollback()
                return TransactionResult.RECEIVER_INSUFFICIENT_FUNDS
            }
        }

        return TransactionResult.SUCCESS(transaction)
    }

    /**
     * Transfer money from one user to another transactionally
     *
     * The transactions will be persisted if the transfer was successful
     * If there was an error during the transfer, both transactions will be rolled back
     *
     * @param senderTransaction The transaction of the sender
     * @param receiverTransaction The transaction of the receiver
     *
     * @return The result of the transfer
     */
    suspend fun transfer(
        senderTransaction: Transaction,
        receiverTransaction: Transaction
    ): TransactionResult {
        val senderResult = persistTransaction(senderTransaction)
        if (!senderResult.success) {
            TransactionManager.current().rollback()
            return if (senderResult == TransactionResult.RECEIVER_INSUFFICIENT_FUNDS) TransactionResult.SENDER_INSUFFICIENT_FUNDS else senderResult
        }

        val receiverResult = persistTransaction(receiverTransaction)
        if (!receiverResult.success) {
            TransactionManager.current().rollback()
            return receiverResult
        }

        return TransactionResult.TRANSFER_SUCCESS(senderTransaction, receiverTransaction)
    }
}