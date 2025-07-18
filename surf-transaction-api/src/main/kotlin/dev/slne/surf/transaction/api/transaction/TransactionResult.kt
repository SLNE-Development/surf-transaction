package dev.slne.surf.transaction.api.transaction

typealias TransactionResultType = Pair<TransactionResult, Transaction?>

@Suppress("ClassName")
sealed class TransactionResult(val message: String? = null) {

    /**
     * Transaction was successful
     */
    data object SUCCESS : TransactionResult(null)

    /**
     * The receiver of the transaction has insufficient funds
     */
    data object RECEIVER_INSUFFICIENT_FUNDS : TransactionResult("The receiver has insufficient funds to complete this transaction.")

    /**
     * The sender of the transaction has insufficient funds
     */
    data object SENDER_INSUFFICIENT_FUNDS : TransactionResult("The sender has insufficient funds to complete this transaction.")

    /**
     * There was an error with the database
     */
    data class DATABASE_ERROR(val cause: Throwable) : TransactionResult("An error occurred with the database.")
}