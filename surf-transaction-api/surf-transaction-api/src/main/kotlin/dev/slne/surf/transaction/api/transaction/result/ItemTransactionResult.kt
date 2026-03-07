package dev.slne.surf.transaction.api.transaction.result

import dev.slne.surf.transaction.api.item.ItemTransaction

/**
 * Extension of [TransactionResult] with item-specific failure reasons.
 */
sealed class ItemTransactionResult(success: Boolean = false) : TransactionResult(success) {

    /**
     * The item transaction completed successfully.
     */
    data class Success(val transaction: ItemTransaction) : ItemTransactionResult(true)

    /**
     * A transfer of items completed successfully.
     */
    data class TransferSuccess(
        val senderTransaction: ItemTransaction,
        val receiverTransaction: ItemTransaction
    ) : ItemTransactionResult(true)

    /**
     * The account does not hold enough items.
     */
    data object InsufficientItems : ItemTransactionResult()

    /**
     * The requested amount was invalid (zero or negative).
     */
    data object InvalidAmount : ItemTransactionResult()

    /**
     * The sender and receiver item fingerprints do not match.
     */
    data object FingerprintMismatch : ItemTransactionResult()

    /**
     * A database error occurred during item transaction processing.
     */
    data class DatabaseError(val cause: Throwable) : ItemTransactionResult()
}