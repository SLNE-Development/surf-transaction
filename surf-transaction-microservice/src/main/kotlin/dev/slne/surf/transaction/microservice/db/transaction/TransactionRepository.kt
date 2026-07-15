package dev.slne.surf.transaction.microservice.db.transaction

import dev.slne.surf.transaction.api.transaction.*
import dev.slne.surf.transaction.core.common.transaction.TransactionImpl
import java.math.BigDecimal
import java.util.*

/**
 * Persistence entry point for financial transactions.
 *
 * Transactions are stored append-only: rows are never updated except for their lifecycle
 * [dev.slne.surf.transaction.api.transaction.TransactionState]. An account's balance is the sum
 * of all effective transaction rows credited to it; pending transactions with a negative amount
 * act as reservations until they are committed, rolled back, or expire.
 */
interface TransactionRepository {

    /** Persists a single, immediately committed transaction. */
    suspend fun persistTransaction(transaction: TransactionImpl): TransactionResult

    /** Persists a single pending reservation that expires after [timeoutMillis]. */
    suspend fun persistPendingTransaction(
        transaction: TransactionImpl,
        timeoutMillis: Long
    ): PendingTransactionResult

    /** Persists both sides of a transfer as one atomic, immediately committed operation. */
    suspend fun transfer(
        senderTransaction: TransactionImpl,
        receiverTransaction: TransactionImpl
    ): TransactionResult

    /** Persists both sides of a transfer as one atomic pending reservation. */
    suspend fun pendingTransfer(
        senderTransaction: TransactionImpl,
        receiverTransaction: TransactionImpl,
        timeoutMillis: Long
    ): PendingTransactionResult

    /** Commits the pending transaction [identifier], including a linked transfer side. */
    suspend fun commit(identifier: UUID): TransactionCommitResult

    /** Rolls back the pending transaction [identifier], including a linked transfer side. */
    suspend fun rollback(identifier: UUID): TransactionRollbackResult

    suspend fun find(identifier: UUID): Transaction?

    /** Persists the expired state for all overdue reservations; returns the row count. */
    suspend fun expireTransactions(): Int

    suspend fun balanceDecimal(accountId: UUID, currencyName: String): BigDecimal

    companion object : TransactionRepository by TransactionRepositoryImpl()
}
