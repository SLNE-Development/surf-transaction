package dev.slne.surf.transaction.api.transaction

import net.kyori.adventure.util.Services
import java.util.*

/**
 * A service to handle transactions
 */
interface TransactionService {

    /**
     * Execute a transaction
     *
     * @param transaction The transaction to execute
     *
     * @return The result of the transaction
     */
    suspend fun persistTransaction(transaction: Transaction): TransactionResult

    /**
     * Rollback a transaction
     *
     * The transaction will be rollbacked by createing a new transaction that is negative the amount of the original transaction
     *
     * @param transaction The transaction to rollback
     * @return The result of the rollback and the rollback transaction
     */
    suspend fun rollbackTransaction(transaction: Transaction): Pair<TransactionResult, Transaction?>

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
    ): TransactionResult

    /**
     * Generate a new transaction id
     *
     * @return The generated transaction id
     */
    suspend fun generateTransactionId(): UUID

    companion object {
        val INSTANCE: TransactionService =
            Services.serviceWithFallback(TransactionService::class.java).orElseThrow {
                IllegalStateException("No implementation of TransactionService found")
            }
    }

}

val transactionService: TransactionService
    get() = TransactionService.INSTANCE