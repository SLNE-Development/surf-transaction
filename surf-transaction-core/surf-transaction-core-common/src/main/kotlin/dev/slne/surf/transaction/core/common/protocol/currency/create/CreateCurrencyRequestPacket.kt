package dev.slne.surf.transaction.core.common.protocol.currency.create

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.transaction.core.common.currency.CurrencyImpl
import kotlinx.serialization.Serializable

@Serializable
data class CreateCurrencyRequestPacket(val currency: CurrencyImpl) : RabbitRequestPacket<CreateCurrencyResponsePacket>()
