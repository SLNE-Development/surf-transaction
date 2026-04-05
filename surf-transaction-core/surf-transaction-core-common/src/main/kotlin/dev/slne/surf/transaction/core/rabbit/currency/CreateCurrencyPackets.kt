package dev.slne.surf.transaction.core.rabbit.currency

import dev.slne.surf.rabbitmq.packet.RabbitRequestPacket
import dev.slne.surf.rabbitmq.packet.RabbitResponsePacket
import dev.slne.surf.transaction.core.currency.CurrencyCreateResult
import dev.slne.surf.transaction.core.currency.CurrencyImpl
import kotlinx.serialization.Serializable

@Serializable
class CreateCurrencyRequest(val currency: CurrencyImpl) :
    RabbitRequestPacket<CreateCurrencyResponse>()

@Serializable
class CreateCurrencyResponse(val result: CurrencyCreateResult) : RabbitResponsePacket()
