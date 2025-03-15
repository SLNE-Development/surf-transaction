package dev.slne.surf.transaction.fallback.currency

import com.google.auto.service.AutoService
import dev.slne.surf.surfapi.core.api.util.freeze
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.core.currency.CURRENCY_NAME_MAX_LENGTH
import dev.slne.surf.transaction.core.currency.CURRENCY_SYMBOL_MAX_LENGTH
import dev.slne.surf.transaction.core.currency.CurrencyCreateResult
import dev.slne.surf.transaction.core.currency.CurrencyService
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.Dispatchers
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.util.Services.Fallback
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

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

    override suspend fun createCurrency(currency: Currency): CurrencyCreateResult =
        newSuspendedTransaction(Dispatchers.IO) {
            val existingCurrency =
                FallbackCurrency.find { FallbackCurrencyTable.name eq currency.name }.singleOrNull()

            if (existingCurrency != null) {
                return@newSuspendedTransaction CurrencyCreateResult.ALREADY_EXISTS
            }

            if (currency.defaultCurrency) {
                val defaultCurrency =
                    FallbackCurrency.find { FallbackCurrencyTable.defaultCurrency eq true }
                        .singleOrNull()

                if (defaultCurrency != null) {
                    return@newSuspendedTransaction CurrencyCreateResult.DEFAULT_ALREADY_EXISTS
                }
            }

            val currencyName = currency.name
            if (currencyName.isBlank() || currencyName.length > CURRENCY_NAME_MAX_LENGTH) {
                return@newSuspendedTransaction CurrencyCreateResult.INVALID_NAME
            }

            val currencyDisplayName = currency.displayName
            val plainCurrencyDisplayName =
                PlainTextComponentSerializer.plainText().serialize(currencyDisplayName)

            if (plainCurrencyDisplayName.isBlank() || plainCurrencyDisplayName.length > CURRENCY_NAME_MAX_LENGTH) {
                return@newSuspendedTransaction CurrencyCreateResult.INVALID_NAME
            }

            val currencySymbol = currency.symbol
            if (currencySymbol.isBlank() || currencySymbol.length > CURRENCY_SYMBOL_MAX_LENGTH) {
                return@newSuspendedTransaction CurrencyCreateResult.INVALID_SYMBOL
            }

            val currencySymbolDisplay = currency.symbolDisplay
            val plainCurrencySymbolDisplay =
                PlainTextComponentSerializer.plainText().serialize(currencySymbolDisplay)

            if (plainCurrencySymbolDisplay.isBlank() || plainCurrencySymbolDisplay.length > CURRENCY_SYMBOL_MAX_LENGTH) {
                return@newSuspendedTransaction CurrencyCreateResult.INVALID_SYMBOL
            }

            val newCurrency = FallbackCurrency.new {
                name = currency.name
                displayName = currency.displayName
                symbol = currency.symbol
                symbolDisplay = currency.symbolDisplay
                scale = currency.scale
                defaultCurrency = currency.defaultCurrency
            }

            _fallbackCurrencies.add(newCurrency)
            _currencies.add(newCurrency.toCurrency())

            return@newSuspendedTransaction CurrencyCreateResult.SUCCESS
        }

    override fun getCurrencyByName(name: String) =
        currencies.find { it.name.lowercase() == name.lowercase() }
}