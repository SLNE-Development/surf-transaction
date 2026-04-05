package dev.slne.surf.transaction.core.common.protocol.transaction

import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import dev.slne.surf.transaction.api.transaction.TransactionResult
import kotlinx.serialization.Serializable

@Serializable
data class TransactionResultPacket(val result: TransactionResult) : RabbitResponsePacket()