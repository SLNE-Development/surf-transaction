package dev.slne.surf.transaction.microservice.handler.currency

import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import dev.slne.surf.transaction.core.common.protocol.currency.makeDefaultCurrency.MakeDefaultCurrencyRequestPacket
import dev.slne.surf.transaction.core.common.protocol.currency.makeDefaultCurrency.MakeDefaultCurrencyResponsePacket
import dev.slne.surf.transaction.microservice.db.currency.CurrencyRepository
import kotlinx.coroutines.launch

object MakeDefaultCurrencyHandler {

    @RabbitHandler
    fun handleMakeDefaultCurrency(request: MakeDefaultCurrencyRequestPacket) {
        request.launch {
            val result = CurrencyRepository.makeDefaultCurrency(request.currencyName)
            request.respond(MakeDefaultCurrencyResponsePacket(result))
        }
    }
}