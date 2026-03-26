package dev.slne.surf.transaction.core.common.protocol.transaction.transfer

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.transaction.core.common.protocol.transaction.TransactionResultPacket
import dev.slne.surf.transaction.core.common.transaction.TransactionImpl
import kotlinx.serialization.Serializable

@Serializable
data class TransferTransactionCreateRequestPacket(
    val senderTransaction: TransactionImpl,
    val receiverTransaction: TransactionImpl
) : RabbitRequestPacket<TransactionResultPacket>()