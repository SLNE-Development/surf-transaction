package dev.slne.surf.transaction.core.common.protocol.transaction.create

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.transaction.core.common.protocol.transaction.TransactionResultPacket
import dev.slne.surf.transaction.core.common.transaction.TransactionImpl
import kotlinx.serialization.Serializable

@Serializable
data class CreateTransactionRequestPacket(val transaction: TransactionImpl) :
    RabbitRequestPacket<TransactionResultPacket>()
