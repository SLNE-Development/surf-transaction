package dev.slne.surf.transaction.core.client.currency

import dev.slne.surf.cloud.api.common.util.freeze
import dev.slne.surf.cloud.api.common.util.mutableObjectSetOf
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.currency.InternalCurrencyBridge
import dev.slne.surf.transaction.core.currency.CurrencyImpl
import org.springframework.stereotype.Component

@Component
class ClientCurrencyBridge : InternalCurrencyBridge {
    private var _defaultCurrency: CurrencyImpl? = null

    override val defaultCurrency: Currency
        get() = _defaultCurrency ?: error("Transaction api is not initialized yet.")

    private val _currencies = mutableObjectSetOf<CurrencyImpl>()
    override val currencies = _currencies.freeze()

    fun updateCurrencies(currencies: Set<CurrencyImpl>) {
        _currencies.clear()
        _currencies.addAll(currencies)
        _defaultCurrency = currencies.find { it.defaultCurrency } ?: CurrencyImpl.DEFAULT
    }
}