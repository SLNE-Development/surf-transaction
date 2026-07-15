package dev.slne.surf.transaction.api.transaction

import kotlinx.serialization.Serializable

/**
 * The typed result of rolling back a persisted pending financial transaction.
 *
 * This lifecycle rollback is distinct from rolling back a short-lived database transaction: the
 * financial transaction remains stored as an audit record in [TransactionState.ROLLED_BACK].
 */
@Serializable
sealed class TransactionRollbackResult {

    /**
     * The pending [transaction] was atomically rolled back and its reservation was released.
     */
    @Serializable
    data class RolledBack(val transaction: Transaction) : TransactionRollbackResult()

    /**
     * The [transaction] was already rolled back; the requested final state is already durable.
     */
    @Serializable
    data class AlreadyRolledBack(val transaction: Transaction) : TransactionRollbackResult()

    /** The [transaction] was already committed and cannot be rolled back. */
    @Serializable
    data class Committed(val transaction: Transaction) : TransactionRollbackResult()

    /** The [transaction] had already expired and no longer held a reservation. */
    @Serializable
    data class Expired(val transaction: Transaction) : TransactionRollbackResult()

    /** No transaction with the requested identifier exists. */
    @Serializable
    data object NotFound : TransactionRollbackResult()

    /** The [transaction] had a state that is not valid for this transition. */
    @Serializable
    data class InvalidState(val transaction: Transaction) : TransactionRollbackResult()
}
