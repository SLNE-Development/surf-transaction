package dev.slne.surf.transaction.api.transaction

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Result wrapper returned by transaction operations.
 *
 * Each subtype indicates a distinct outcome and carries a human-readable [message]
 * suitable for logs.
 *
 * ### Serialization
 * * The sealed hierarchy is `@Serializable`; polymorphic serializers can be used
 *   to transmit results over the network.
 * * For [DATABASE_ERROR],
 * the underlying [dev.slne.surf.transaction.api.transaction.TransactionResult.DATABASE_ERROR.cause] is marked
 * `@Transient` and is **not** serialized.
 *
 * @property message localized or default text describing the outcome
 */
@Serializable
@Suppress("ClassName")
sealed class TransactionResult(val message: String) {

    /**
     * Indicates that the transaction executed without issues.
     *
     * @property transaction the completed transaction instance
     */
    data class SUCCESS(val transaction: SerializableTransaction) :
        TransactionResult("Transaction completed successfully.")

    /**
     * The receiver lacked sufficient balance to accept the transfer.
     */
    data object RECEIVER_INSUFFICIENT_FUNDS :
        TransactionResult("The receiver has insufficient funds to complete this transaction.")

    /**
     * The sender lacked sufficient balance to perform the transfer.
     */
    data object SENDER_INSUFFICIENT_FUNDS :
        TransactionResult("The sender has insufficient funds to complete this transaction.")

    /**
     * An unrecoverable persistence-layer error occurred while processing the transaction.
     *
     * @param cause root exception; excluded from serialization via [Transient]
     */
    data class DATABASE_ERROR(@Transient val cause: Throwable = Throwable("Unknown cause")) :
        TransactionResult("An error occurred with the database.")
}