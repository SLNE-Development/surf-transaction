package dev.slne.transaction.api.transaction

enum class TransactionResult {

    /**
     * Transaction was successful
     */
    SUCCESS,

    /**
     * The receiver of the transaction has insufficient funds
     */
    RECEIVER_INSUFFICIENT_FUNDS,

    /**
     * The sender of the transaction has insufficient funds
     */
    SENDER_INSUFFICIENT_FUNDS,

    /**
     * There was an error with the database
     */
    DATABASE_ERROR,

    /**
     * The transaction ID was not generatable
     */
    TRANSACTION_ID_NOT_GENERATABLE,

    /**
     * An unknown error occurred
     */
    UNKNOWN_ERROR

}