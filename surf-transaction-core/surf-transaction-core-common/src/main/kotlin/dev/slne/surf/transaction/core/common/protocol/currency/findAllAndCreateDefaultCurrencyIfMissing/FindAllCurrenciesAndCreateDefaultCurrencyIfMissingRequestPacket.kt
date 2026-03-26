package dev.slne.surf.transaction.core.common.protocol.currency.findAllAndCreateDefaultCurrencyIfMissing

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Serializable

@Serializable
class FindAllCurrenciesAndCreateDefaultCurrencyIfMissingRequestPacket :
    RabbitRequestPacket<FindAllCurrenciesAndCreateDefaultCurrencyIfMissingResponsePacket>()
