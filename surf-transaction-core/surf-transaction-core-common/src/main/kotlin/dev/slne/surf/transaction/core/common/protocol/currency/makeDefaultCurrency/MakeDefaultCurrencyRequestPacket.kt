package dev.slne.surf.transaction.core.common.protocol.currency.makeDefaultCurrency

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Serializable

@Serializable
data class MakeDefaultCurrencyRequestPacket(val currencyName: String) :
    RabbitRequestPacket<MakeDefaultCurrencyResponsePacket>()