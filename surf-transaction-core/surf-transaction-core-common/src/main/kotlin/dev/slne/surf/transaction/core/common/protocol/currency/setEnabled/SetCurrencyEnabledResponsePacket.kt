package dev.slne.surf.transaction.core.common.protocol.currency.setEnabled

import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import dev.slne.surf.transaction.core.common.currency.CurrencyEnabledResult
import kotlinx.serialization.Serializable

@Serializable
data class SetCurrencyEnabledResponsePacket(val result: CurrencyEnabledResult) :
    RabbitResponsePacket()
