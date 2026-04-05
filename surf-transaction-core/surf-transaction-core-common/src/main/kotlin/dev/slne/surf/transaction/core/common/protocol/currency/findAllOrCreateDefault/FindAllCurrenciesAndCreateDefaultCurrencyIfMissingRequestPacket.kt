package dev.slne.surf.transaction.core.common.protocol.currency.findAllOrCreateDefault

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Serializable

@Serializable
class FindAllCurrenciesAndCreateDefaultCurrencyIfMissingRequestPacket :
    RabbitRequestPacket<FindAllCurrenciesAndCreateDefaultCurrencyIfMissingResponsePacket>()