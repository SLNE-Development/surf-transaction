package dev.slne.surf.transaction.fallback.currency

import com.google.auto.service.AutoService
import dev.slne.surf.surfapi.core.api.util.freeze
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.core.currency.CurrencyService
import it.unimi.dsi.fastutil.objects.ObjectSet
import net.kyori.adventure.util.Services.Fallback

@AutoService(CurrencyService::class)
class FallbackCurrencyService : CurrencyService, Fallback {

    private val _fallbackCurrencies = mutableObjectSetOf<FallbackCurrency>()
    val fallbackCurrencies get() = _fallbackCurrencies.freeze()

    private val _currencies = mutableObjectSetOf<Currency>()
    override val currencies get() = _currencies.freeze()

    override suspend fun fetchCurrencies(): ObjectSet<Currency> {
        val fetchedCurrencies = FallbackCurrency.all()

        _fallbackCurrencies.clear()
        _fallbackCurrencies.addAll(fetchedCurrencies)

        _currencies.clear()
        _currencies.addAll(fetchedCurrencies.map {
            it.toCurrency()
        })

        return currencies
    }

    override fun getCurrencyByName(name: String) =
        currencies.find { it.name.lowercase() == name.lowercase() }
}