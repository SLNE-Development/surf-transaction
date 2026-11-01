package dev.slne.surf.transaction.core.common.protocol.currency.setEnabled

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Serializable

@Serializable
data class SetCurrencyEnabledRequestPacket(val currencyName: String, val enabled: Boolean) :
    RabbitRequestPacket<SetCurrencyEnabledResponsePacket>()
