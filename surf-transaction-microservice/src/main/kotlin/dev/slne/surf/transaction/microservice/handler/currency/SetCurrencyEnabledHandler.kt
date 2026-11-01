package dev.slne.surf.transaction.microservice.handler.currency

import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import dev.slne.surf.transaction.core.common.protocol.currency.setEnabled.SetCurrencyEnabledRequestPacket
import dev.slne.surf.transaction.core.common.protocol.currency.setEnabled.SetCurrencyEnabledResponsePacket
import dev.slne.surf.transaction.microservice.db.currency.CurrencyRepository
import kotlinx.coroutines.launch

object SetCurrencyEnabledHandler {

    @RabbitHandler
    fun handleSetCurrencyEnabled(request: SetCurrencyEnabledRequestPacket) {
        request.launch {
            val result = CurrencyRepository.setCurrencyEnabled(request.currencyName, request.enabled)
            request.respond(SetCurrencyEnabledResponsePacket(result))
        }
    }
}
