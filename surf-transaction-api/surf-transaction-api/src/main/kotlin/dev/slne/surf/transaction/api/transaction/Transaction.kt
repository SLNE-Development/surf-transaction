package dev.slne.surf.transaction.api.transaction

import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import org.jetbrains.annotations.ApiStatus
import org.jetbrains.annotations.Unmodifiable
import java.util.*

/**
 * Represents a single transaction within the transaction system.
 *
 * A [Transaction] is a base type for all transactional records.
 * Concrete subtypes include [BalanceTransaction] for currency operations
 * and [ItemTransaction] for item operations.
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
}
