package dev.slne.surf.transaction.microservice.db.transaction.access

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.*
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.javatime.CurrentTimestamp
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.update
import dev.slne.surf.transaction.api.transaction.TransactionState
import dev.slne.surf.transaction.core.common.transaction.TransactionImpl
import dev.slne.surf.transaction.microservice.db.transaction.table.TransactionTable
import java.util.*

/**
 * Moves pending transactions into their terminal state (committed, rolled back, or expired).
 *
 * All updates address the whole operation, so both sides of a pending transfer always
 * transition together.
 */
internal class TransactionLifecycle(private val reader: TransactionReader) {

    /**
     * Attempts to move the pending transaction [identifier] into [targetState].
     *
     * Returns null when no transaction with [identifier] exists. Otherwise the result tells
     * whether this call [applied][LifecycleTransition.applied] the transition; if not, the
     * contained snapshot shows the state that prevented it, such as an already committed or
     * expired transaction.
     */
    suspend fun transition(
        identifier: UUID,
        targetState: TransactionState
    ): LifecycleTransition? {
        require(targetState == TransactionState.COMMITTED || targetState == TransactionState.ROLLED_BACK) { "Cannot transition to $targetState" }
        return suspendTransaction {
            handleTransactionStateChange(identifier, targetState)
        }
    }

    private suspend fun handleTransactionStateChange(
        identifier: UUID,
        targetState: TransactionState
    ): LifecycleTransition? {
        val stored = reader.find(identifier) ?: return null
        val current = stored.currentSnapshot()

        if (current.state != TransactionState.PENDING) {
            // Expiry may only have been determined logically by currentSnapshot()
            if (
                current.state == TransactionState.EXPIRED &&
                stored.transaction.state == TransactionState.PENDING
            ) {
                markExpired(stored)
            }

            return LifecycleTransition(applied = false, snapshot = current)
        }

        // Only one concurrent commit, rollback, or expiry can win
        val updatedRows = TransactionTable.update({
            stored.activePendingOperationPredicate()
        }) {
            it[state] = targetState
        }

        if (updatedRows > 0) {
            return LifecycleTransition(
                applied = true,
                snapshot = current.copy(state = targetState)
            )
        }

        // The reservation either expired or was transitioned concurrently.
        markExpired(stored)

        val latest = reader.find(identifier)?.currentSnapshot() ?: return null

        return LifecycleTransition(applied = false, snapshot = latest)
    }

    suspend fun expireAllDue(): Int = suspendTransaction {
        TransactionTable.update({
            duePendingPredicate()
        }) {
            it[state] = TransactionState.EXPIRED
        }
    }

    private suspend fun markExpired(stored: StoredTransaction) {
        TransactionTable.update({
            stored.duePendingOperationPredicate()
        }) {
            it[state] = TransactionState.EXPIRED
        }
    }

    /**
     * Selects pending transactions whose expiration time is still in the future.
     */
    private fun activePendingPredicate(): Op<Boolean> = listOf(
        TransactionTable.state eq TransactionState.PENDING,
        TransactionTable.expiresAt.isNotNull(),
        TransactionTable.expiresAt greater CurrentTimestamp
    ).compoundAnd()

    /**
     * Selects pending transactions whose expiration time has been reached.
     */
    private fun duePendingPredicate(): Op<Boolean> = listOf(
        TransactionTable.state eq TransactionState.PENDING,
        TransactionTable.expiresAt.isNotNull(),
        TransactionTable.expiresAt lessEq CurrentTimestamp
    ).compoundAnd()

    /**
     * Selects this entire operation while it is pending and unexpired.
     */
    private fun StoredTransaction.activePendingOperationPredicate(): Op<Boolean> {
        return operationSelector() and activePendingPredicate()
    }

    /**
     * Selects this entire operation while it is pending and due for expiry.
     */
    private fun StoredTransaction.duePendingOperationPredicate(): Op<Boolean> {
        return operationSelector() and duePendingPredicate()
    }

    /** Selects the whole operation; legacy rows without one are addressed individually. */
    private fun StoredTransaction.operationSelector(): Op<Boolean> =
        operationId?.let {
            TransactionTable.operationId eq it
        } ?: (TransactionTable.identifier eq transaction.identifier)
}

/** Result of a lifecycle [TransactionLifecycle.transition] attempt on an existing transaction. */
internal data class LifecycleTransition(
    val applied: Boolean,
    val snapshot: TransactionImpl
)
