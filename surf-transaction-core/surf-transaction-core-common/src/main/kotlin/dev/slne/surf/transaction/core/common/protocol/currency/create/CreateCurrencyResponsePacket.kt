package dev.slne.surf.transaction.core.common.protocol.currency.create

import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import dev.slne.surf.transaction.core.common.currency.CurrencyCreateResult
import kotlinx.serialization.Serializable

@Serializable
data class CreateCurrencyResponsePacket(val result: CurrencyCreateResult) : RabbitResponsePacket()
