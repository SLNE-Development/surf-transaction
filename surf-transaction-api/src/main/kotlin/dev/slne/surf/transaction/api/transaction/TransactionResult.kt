package dev.slne.surf.transaction.api.transaction

import dev.slne.surf.api.core.util.SerializableError
import kotlinx.serialization.Serializable

/**
 * Represents the result of a transactional operation.
 *
 * A [TransactionResult] indicates whether a transaction was successful and,
 * if applicable, provides access to the created [Transaction] instances or
 * detailed failure information.
 *
 * @property success whether the transaction completed successfully
 */
@Serializable
sealed class TransactionResult(val success: Boolean = false) {

    /**
     * Indicates that a single transaction completed successfully.
     *
     * This is typically returned for deposits or withdrawals.
     *
     * @param transaction the resulting transaction
     */
    @Serializable
    data class Success(val transaction: Transaction) : TransactionResult(true)

    /**
     * Indicates that a transfer completed successfully.
     *
     * A transfer consists of two linked transactions: one for the sender
     * and one for the receiver.
     *
     * @param senderTransaction the transaction applied to the sender account
     * @param receiverTransaction the transaction applied to the receiver account
     */
    @Serializable
    data class TransferSuccess(
        val senderTransaction: Transaction,
        val receiverTransaction: Transaction
    ) : TransactionResult(true)

    /**
     * Indicates that the receiver account has insufficient funds.
     */
    @Serializable
    data object ReceiverInsufficientFunds : TransactionResult()

    /**
     * Indicates that the sender account has insufficient funds.
     */
    @Serializable
    data object SenderInsufficientFunds : TransactionResult()

    /**
     * Indicates that a database error occurred during transaction processing.
     *
     * @param cause the underlying error
     */
    @Serializable
    data class DatabaseError(val cause: SerializableError) : TransactionResult()
}
