package dev.slne.surf.transaction.api.transaction

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

typealias TransactionResultType = Pair<TransactionResult, Transaction?>

@Serializable
@Suppress("ClassName")
sealed class TransactionResult(val message: String? = null) {

    /**
     * Transaction was successful
     */
    data object SUCCESS : TransactionResult(null)

    /**
     * The receiver of the transaction has insufficient funds
     */
    data object RECEIVER_INSUFFICIENT_FUNDS :
        TransactionResult("The receiver has insufficient funds to complete this transaction.")

    /**
     * The sender of the transaction has insufficient funds
     */
    data object SENDER_INSUFFICIENT_FUNDS :
        TransactionResult("The sender has insufficient funds to complete this transaction.")

    /**
     * There was an error with the database
     */
    data class DATABASE_ERROR(@Transient val cause: Throwable = Throwable("Unknown cause")) :
        TransactionResult("An error occurred with the database.")
}