package dev.slne.surf.transaction.api.transaction


sealed class TransactionResult(val success: Boolean = false) {

    data class Success(val transaction: Transaction) : TransactionResult(true)

    data class TransferSuccess(
        val senderTransaction: Transaction,
        val receiverTransaction: Transaction
    ) : TransactionResult(true)

    data object ReceiverInsufficientFunds : TransactionResult()
    data object SenderInsufficientFunds : TransactionResult()
    data class DatabaseError(val cause: Throwable) : TransactionResult()
}