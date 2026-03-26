package dev.slne.surf.transaction.microservice.handler.currency

import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import dev.slne.surf.transaction.core.common.protocol.currency.findAllAndCreateDefaultCurrencyIfMissing.FindAllCurrenciesAndCreateDefaultCurrencyIfMissingRequestPacket
import dev.slne.surf.transaction.core.common.protocol.currency.findAllAndCreateDefaultCurrencyIfMissing.FindAllCurrenciesAndCreateDefaultCurrencyIfMissingResponsePacket
import dev.slne.surf.transaction.microservice.db.currency.CurrencyRepository
import kotlinx.coroutines.launch

object FindAllCurrenciesAndCreateDefaultCurrencyIfMissingHandler {

    @RabbitHandler
    fun handleFindAllCurrenciesAndCreateDefaultCurrencyIfMissing(request: FindAllCurrenciesAndCreateDefaultCurrencyIfMissingRequestPacket) {
        request.launch {
            val currencies = CurrencyRepository.findAllAndCreateDefaultCurrencyIfMissing()
            request.respond(FindAllCurrenciesAndCreateDefaultCurrencyIfMissingResponsePacket(currencies))
        }
    }
}