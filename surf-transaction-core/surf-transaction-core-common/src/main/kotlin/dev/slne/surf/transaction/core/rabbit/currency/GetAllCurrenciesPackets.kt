package dev.slne.surf.transaction.core.rabbit.currency

import dev.slne.surf.rabbitmq.packet.RabbitRequestPacket
import dev.slne.surf.rabbitmq.packet.RabbitResponsePacket
import dev.slne.surf.transaction.core.currency.CurrencyImpl
import kotlinx.serialization.Serializable

@Serializable
class GetAllCurrenciesRequest : RabbitRequestPacket<GetAllCurrenciesResponse>()

@Serializable
class GetAllCurrenciesResponse(
    val currencies: List<CurrencyImpl>,
    val defaultCurrencyName: String
) : RabbitResponsePacket()
