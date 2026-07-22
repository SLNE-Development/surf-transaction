package dev.slne.surf.transaction.api.transaction

import kotlinx.serialization.Serializable

/**
 * The durable lifecycle state of a financial [Transaction].
 *
 * Existing and immediate transactions are [COMMITTED]. A [PENDING] transaction is a reservation
 * that can be committed or rolled back. [ROLLED_BACK] and [EXPIRED] transactions remain available
 * as audit records but no longer reserve or contribute funds.
 */
@Serializable
enum class TransactionState {
    /** The transaction is reserved and may still be committed or rolled back. */
    PENDING,

    /** The transaction has been finalized and contributes to the committed ledger. */
    COMMITTED,

    /** The pending transaction was explicitly rolled back. */
    ROLLED_BACK,

    /** The pending transaction reached its persistent expiration time before being committed. */
    EXPIRED
}
