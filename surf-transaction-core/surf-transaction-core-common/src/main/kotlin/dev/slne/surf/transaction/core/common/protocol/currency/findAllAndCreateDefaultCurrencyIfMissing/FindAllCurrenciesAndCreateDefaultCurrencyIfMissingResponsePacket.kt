package dev.slne.surf.transaction.core.common.protocol.currency.findAllAndCreateDefaultCurrencyIfMissing

import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import dev.slne.surf.transaction.core.common.currency.CurrencyImpl
import kotlinx.serialization.Serializable

@Serializable
data class FindAllCurrenciesAndCreateDefaultCurrencyIfMissingResponsePacket(val currencies: List<CurrencyImpl>) :
    RabbitResponsePacket()
