package dev.slne.surf.transaction.core.common.protocol.transaction.balance

import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@Serializable
data class GetTransactionBalanceResponsePacket(val balance: @Contextual BigDecimal) : RabbitResponsePacket()