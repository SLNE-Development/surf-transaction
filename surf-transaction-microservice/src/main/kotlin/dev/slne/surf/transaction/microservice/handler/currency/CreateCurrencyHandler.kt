package dev.slne.surf.transaction.microservice.handler.currency

import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import dev.slne.surf.transaction.core.common.protocol.currency.create.CreateCurrencyRequestPacket
import dev.slne.surf.transaction.core.common.protocol.currency.create.CreateCurrencyResponsePacket
import dev.slne.surf.transaction.microservice.db.currency.CurrencyRepository
import kotlinx.coroutines.launch

object CreateCurrencyHandler {

    @RabbitHandler
    fun handleCreateCurrency(request: CreateCurrencyRequestPacket) {
        request.launch {
            val result = CurrencyRepository.createCurrency(request.currency)
            request.respond(CreateCurrencyResponsePacket(result))
        }
    }
}