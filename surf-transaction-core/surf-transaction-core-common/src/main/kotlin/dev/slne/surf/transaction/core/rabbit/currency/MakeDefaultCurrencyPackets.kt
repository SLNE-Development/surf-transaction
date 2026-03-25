package dev.slne.surf.transaction.core.rabbit.currency

import dev.slne.surf.rabbitmq.packet.RabbitRequestPacket
import dev.slne.surf.rabbitmq.packet.RabbitResponsePacket
import dev.slne.surf.transaction.core.currency.CurrencyDefaultResult
import kotlinx.serialization.Serializable

@Serializable
class MakeDefaultCurrencyRequest(val currencyName: String) :
    RabbitRequestPacket<MakeDefaultCurrencyResponse>()

@Serializable
class MakeDefaultCurrencyResponse(val result: CurrencyDefaultResult) : RabbitResponsePacket()
