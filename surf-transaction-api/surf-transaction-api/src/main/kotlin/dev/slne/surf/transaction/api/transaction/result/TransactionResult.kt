package dev.slne.surf.transaction.api.transaction.result

/**
 * Represents the result of a transactional operation.
 *
 * A [TransactionResult] indicates whether a transaction was successful.
 * Concrete subtypes provide additional detail for currency transactions
 * ([BalanceTransactionResult]) and item transactions ([dev.slne.surf.transaction.api.transaction.result.ItemTransactionResult]).
 *
 * @property success whether the transaction completed successfully
 */
sealed class TransactionResult(val success: Boolean = false)

