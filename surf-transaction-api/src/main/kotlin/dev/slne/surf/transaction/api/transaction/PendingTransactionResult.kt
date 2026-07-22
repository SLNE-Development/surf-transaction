package dev.slne.surf.transaction.api.transaction

import dev.slne.surf.api.core.util.SerializableError
import kotlinx.serialization.Serializable

/**
 * The result of atomically creating a durable pending transaction reservation.
 */
@Serializable
sealed class PendingTransactionResult {

    /**
     * A single pending deposit or withdrawal was created.
     *
     * @property transaction the immutable pending transaction snapshot
     */
    @Serializable
    data class Created(val transaction: Transaction) : PendingTransactionResult()

    /**
     * Both sides of a pending transfer were created as one atomic reservation.
     *
     * Calling `commit` or `rollback` on either transaction transitions both sides atomically.
     *
     * @property senderTransaction the reserved withdrawal from the sender
     * @property receiverTransaction the deferred deposit to the receiver
     */
    @Serializable
    data class TransferCreated(
        val senderTransaction: Transaction,
        val receiverTransaction: Transaction
    ) : PendingTransactionResult()

    /** The receiver-side account would fall below the configured currency minimum. */
    @Serializable
    data object ReceiverInsufficientFunds : PendingTransactionResult()

    /** The transfer sender would fall below the configured currency minimum. */
    @Serializable
    data object SenderInsufficientFunds : PendingTransactionResult()

    /** The supplied timeout was not finite or could not represent at least one millisecond. */
    @Serializable
    data object InvalidTimeout : PendingTransactionResult()

    /**
     * The reservation request was internally inconsistent or referenced missing data.
     *
     * Examples include an identifier collision, a missing account or currency, or malformed
     * transfer sides. Infrastructure and database failures are thrown by the RPC call instead.
     *
     * @property cause stable validation code and description
     */
    @Serializable
    data class InvalidRequest(val cause: SerializableError) : PendingTransactionResult()
}
