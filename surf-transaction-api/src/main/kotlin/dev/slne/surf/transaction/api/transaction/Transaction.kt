package dev.slne.surf.transaction.api.transaction

import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.Unmodifiable
import java.math.BigDecimal
import java.time.Instant
import java.util.*

/**
 * Represents a single transaction within the transaction system.
 *
 * A [Transaction] describes the transfer of a monetary [amount] in a specific
 * [currency], including optional sender and receiver accounts, metadata, and
 * configuration flags.
 */
@OptIn(InternalTransactionApi::class)
@ApiStatus.NonExtendable
interface Transaction {

    /**
     * The unique identifier of this transaction.
     */
    val identifier: UUID

    /**
     * The UUID of the entity that initiated this transaction.
     *
     * May be `null` if the transaction is system-initiated.
     */
    val initiator: UUID?

    /**
     * The account ID of the sender.
     *
     * This is `null` for transactions without a sender
     * (e.g. deposits).
     */
    val senderAccountId: UUID?

    /**
     * The account ID of the receiver.
     *
     * This is `null` for transactions without a receiver
     * (e.g. withdrawals).
     */
    val receiverAccountId: UUID?

    /**
     * The currency used in this transaction.
     */
    val currency: Currency

    /**
     * The monetary amount of this transaction.
     */
    val amount: BigDecimal

    /**
     * Whether the minimum currency amount check was ignored for this transaction.
     */
    val ignoreMinimumAmount: Boolean

    /**
     * The durable lifecycle state of this transaction snapshot.
     *
     * Transaction objects are immutable. Use [refresh] to obtain a newer snapshot after another
     * process may have committed, rolled back, or expired the transaction.
     */
    val state: TransactionState

    /**
     * The absolute expiration time of a pending reservation.
     *
     * This is `null` for immediate and legacy committed transactions. Expiration is enforced by the
     * database-backed service even when cleanup has not yet changed [state] to
     * [TransactionState.EXPIRED].
     */
    val expiresAt: Instant?

    /**
     * Additional immutable metadata associated with this transaction.
     */
    val data: @Unmodifiable Set<TransactionData>

    /**
     * Resolves and returns the receiver account of this transaction.
     *
     * @return the receiver account, or `null` if none exists or it cannot be resolved
     */
    suspend fun receiverAccount(): Account?

    /**
     * Resolves and returns the sender account of this transaction.
     *
     * @return the sender account, or `null` if none exists or it cannot be resolved
     */
    suspend fun senderAccount(): Account?

    /**
     * Commits this persisted pending transaction.
     *
     * For a transfer, invoking this method on either side commits both sides atomically. Repeating
     * the call is safe and returns [TransactionCommitResult.AlreadyCommitted] once committed.
     * Exceptions raised by the authoritative microservice or the RPC transport are propagated.
     */
    suspend fun commit(): TransactionCommitResult

    /**
     * Rolls back this persisted pending financial transaction and releases its reservation.
     *
     * This does not delete the audit record and is not the same operation as rolling back a database
     * transaction. For a transfer, invoking this method on either side rolls back both sides
     * atomically. Exceptions raised by the authoritative microservice or the RPC transport are
     * propagated.
     */
    suspend fun rollback(): TransactionRollbackResult

    /**
     * Loads the current immutable state of this transaction from the authoritative service.
     *
     * This method never mutates the current object. Exceptions raised by the authoritative
     * microservice or the RPC transport are propagated.
     *
     * @return the current transaction snapshot, or `null` if it no longer exists
     */
    suspend fun refresh(): Transaction?

    companion object {
        /**
         * Loads a transaction by its stable public [identifier].
         *
         * This is the reconciliation entry point when a caller retained an identifier across a
         * process restart or received an indeterminate transport result. Exceptions raised by the
         * authoritative microservice or the RPC transport are propagated.
         *
         * @return the current transaction snapshot, or `null` when [identifier] is unknown
         */
        suspend fun byIdentifier(identifier: UUID): Transaction? =
            TransactionService.find(identifier)
    }
}
