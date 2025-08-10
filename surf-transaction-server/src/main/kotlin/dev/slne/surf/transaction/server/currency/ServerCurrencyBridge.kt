package dev.slne.surf.transaction.server.currency

import dev.slne.surf.transaction.api.currency.InternalCurrencyBridge
import org.springframework.stereotype.Component

@Component
class ServerCurrencyBridge(private val currencyService: CurrencyService) : InternalCurrencyBridge {
    override val defaultCurrency get() = currencyService.defaultCurrency
    override val currencies get() = currencyService.currencies
}