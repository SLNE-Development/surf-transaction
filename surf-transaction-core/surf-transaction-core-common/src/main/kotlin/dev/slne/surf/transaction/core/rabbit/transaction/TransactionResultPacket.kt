package dev.slne.surf.transaction.core.rabbit.transaction

import dev.slne.surf.transaction.core.transaction.TransactionImpl
import kotlinx.serialization.Serializable

@Serializable
sealed class TransactionResultPacket {
    @Serializable
    data class Success(val transaction: TransactionImpl) : TransactionResultPacket()

    @Serializable
    data class TransferSuccess(
        val senderTransaction: TransactionImpl,
        val receiverTransaction: TransactionImpl
    ) : TransactionResultPacket()

    @Serializable
    data object ReceiverInsufficientFunds : TransactionResultPacket()

    @Serializable
    data object SenderInsufficientFunds : TransactionResultPacket()

    @Serializable
    data class DatabaseError(val message: String) : TransactionResultPacket()
}
