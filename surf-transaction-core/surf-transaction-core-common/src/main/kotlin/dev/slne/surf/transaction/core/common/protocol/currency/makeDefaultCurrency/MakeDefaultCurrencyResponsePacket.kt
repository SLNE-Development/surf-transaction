package dev.slne.surf.transaction.core.common.protocol.currency.makeDefaultCurrency

import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import dev.slne.surf.transaction.core.common.currency.CurrencyDefaultResult
import kotlinx.serialization.Serializable

@Serializable
data class MakeDefaultCurrencyResponsePacket(val result: CurrencyDefaultResult) : RabbitResponsePacket()