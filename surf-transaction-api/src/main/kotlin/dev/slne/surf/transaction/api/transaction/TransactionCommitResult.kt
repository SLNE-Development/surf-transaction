package dev.slne.surf.transaction.api.transaction

import kotlinx.serialization.Serializable

/**
 * The typed result of committing a persisted pending financial transaction.
 */
@Serializable
sealed class TransactionCommitResult {

    /** The pending transaction was atomically committed. [transaction] is the committed snapshot. */
    @Serializable
    data class Committed(val transaction: Transaction) : TransactionCommitResult()

    /**
     * The transaction was already committed; the requested final state is already durable.
     * [transaction] is the current snapshot.
     */
    @Serializable
    data class AlreadyCommitted(val transaction: Transaction) : TransactionCommitResult()

    /** The [transaction] was already rolled back and cannot be committed. */
    @Serializable
    data class RolledBack(val transaction: Transaction) : TransactionCommitResult()

    /** The [transaction] expired before commit and cannot be committed. */
    @Serializable
    data class Expired(val transaction: Transaction) : TransactionCommitResult()

    /** No transaction with the requested identifier exists. */
    @Serializable
    data object NotFound : TransactionCommitResult()

    /** The [transaction] had a state that is not valid for this transition. */
    @Serializable
    data class InvalidState(val transaction: Transaction) : TransactionCommitResult()
}
