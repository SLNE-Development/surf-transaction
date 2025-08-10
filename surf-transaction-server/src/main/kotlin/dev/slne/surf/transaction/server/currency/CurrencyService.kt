package dev.slne.surf.transaction.server.currency

import dev.slne.surf.cloud.api.server.netty.packet.broadcast
import dev.slne.surf.surfapi.core.api.util.freeze
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.core.currency.CurrencyCreateResult
import dev.slne.surf.transaction.core.currency.CurrencyImpl
import dev.slne.surf.transaction.core.netty.packets.clientbound.ClientboundRefreshCurrencies
import org.springframework.stereotype.Service

@Service
class CurrencyService(private val currencyRepository: CurrencyRepository) {

    @Volatile
    private var _defaultCurrency: Currency? = null

    val defaultCurrency: Currency
        get() = _defaultCurrency
            ?: error("Currencies are not fetched yet. Call fetchCurrencies() first.")

    private val _currencies = mutableObjectSetOf<CurrencyImpl>()
    val currencies = _currencies.freeze()

    suspend fun cacheCurrencies() {
        val (currencies, defaultCurrency) = currencyRepository.fetchAll()

        this._defaultCurrency = defaultCurrency
        this._currencies.clear()
        this._currencies.addAll(currencies)
    }

    fun getCurrencyByName(name: String): Currency? = _currencies.find { it.name.equals(name, true) }

    suspend fun createCurrency(currency: CurrencyImpl): CurrencyCreateResult {
        val result = currencyRepository.createCurrency(currency, false)

        if (result is CurrencyCreateResult.SUCCESS) {
            if (result.default) {
                _defaultCurrency = result.currency
            }
            _currencies.add(result.currency)

            ClientboundRefreshCurrencies(_currencies).broadcast()
        }

        return result
    }

    suspend fun makeDefaultCurrency(currency: CurrencyImpl): CurrencyCreateResult {
        val result = currencyRepository.createCurrency(currency, true)

        if (result is CurrencyCreateResult.CHANGED_DEFAULT_CURRENCY) {
            _defaultCurrency = currency
            _currencies.forEach { it.defaultCurrency = it.name.equals(defaultCurrency.name, true) }
        }

        if (result is CurrencyCreateResult.SUCCESS) {
            _defaultCurrency = result.currency
            _currencies.add(result.currency)
            _currencies.forEach { it.defaultCurrency = it.name.equals(defaultCurrency.name, true) }
        }

        if (result is CurrencyCreateResult.SUCCESS || result is CurrencyCreateResult.CHANGED_DEFAULT_CURRENCY) {
            ClientboundRefreshCurrencies(_currencies).broadcast()
        }

        return result
    }
}