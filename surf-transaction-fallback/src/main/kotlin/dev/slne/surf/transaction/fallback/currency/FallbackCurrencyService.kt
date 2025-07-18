package dev.slne.surf.transaction.fallback.currency

import com.google.auto.service.AutoService
import dev.slne.surf.surfapi.core.api.util.freeze
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.core.currency.*
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.coroutines.Dispatchers
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import net.kyori.adventure.util.Services.Fallback
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

@AutoService(CurrencyService::class)
class FallbackCurrencyService : CurrencyService, Fallback {

    @Volatile
    private var _defaultCurrency: Currency? = null
    override val defaultCurrency: Currency
        get() = _defaultCurrency
            ?: error("Currencies are not fetched yet. Call fetchCurrencies() first.")

    private val _fallbackCurrencies = mutableObjectSetOf<FallbackCurrency>()
    val fallbackCurrencies = _fallbackCurrencies.freeze()

    private val _currencies = mutableObjectSetOf<CoreCurrency>()
    override val currencies = _currencies.freeze()

    override suspend fun fetchCurrencies(): ObjectSet<out Currency> {
        newSuspendedTransaction(Dispatchers.IO) {
            val fetchedCurrencies = FallbackCurrency.all()

            _fallbackCurrencies.clear()
            _fallbackCurrencies.addAll(fetchedCurrencies)

            _currencies.clear()
            _currencies.addAll(fetchedCurrencies.map {
                it.toCurrency()
            })

            val defaultCurrency = _currencies.find { it.defaultCurrency }
            if (defaultCurrency != null) {
                _defaultCurrency = defaultCurrency
            } else {
                log.atWarning()
                    .log("No default currency found in the database. Creating a fallback default currency...")

                val fallbackDefaultCurrency = FallbackCurrency.new {
                    name = CoreCurrency.DEFAULT.name
                    displayName = CoreCurrency.DEFAULT.displayName
                    symbol = CoreCurrency.DEFAULT.symbol
                    symbolDisplay = CoreCurrency.DEFAULT.symbolDisplay
                    scale = CoreCurrency.DEFAULT.scale
                    this.defaultCurrency = CoreCurrency.DEFAULT.defaultCurrency
                    minimumAmount = CoreCurrency.DEFAULT.minimumAmount
                }

                _fallbackCurrencies.add(fallbackDefaultCurrency)

                val fallbackCurrency = fallbackDefaultCurrency.toCurrency()
                _currencies.add(fallbackCurrency)
                _defaultCurrency = fallbackCurrency
            }
        }

        return currencies
    }

    override suspend fun createCurrency(currency: CoreCurrency): CurrencyCreateResult {
        return createCurrency(currency, false)
    }

    override suspend fun makeDefaultCurrency(currency: CoreCurrency): CurrencyCreateResult {
        return createCurrency(currency, true)
    }

    private suspend fun createCurrency(
        currency: CoreCurrency,
        overrideDefault: Boolean
    ): CurrencyCreateResult =
        newSuspendedTransaction(Dispatchers.IO) {
            val existing = FallbackCurrency
                .find { FallbackCurrencyTable.name eq currency.name }
                .forUpdate()
                .singleOrNull()

            if (existing != null) {
                if (currency.defaultCurrency && overrideDefault) {
                    FallbackCurrency.find { FallbackCurrencyTable.defaultCurrency eq true }
                        .forUpdate()
                        .singleOrNull()
                        ?.defaultCurrency = false

                    existing.defaultCurrency = true
                    _defaultCurrency = currency
                    _currencies.forEach { it.defaultCurrency = it.name.equals(existing.name, true) }
                }
                return@newSuspendedTransaction CurrencyCreateResult.ALREADY_EXISTS
            }


            validate(currency)?.let { return@newSuspendedTransaction it }

            if (currency.defaultCurrency) {
                val previousDefault =
                    FallbackCurrency.find { FallbackCurrencyTable.defaultCurrency eq true }
                        .forUpdate()
                        .singleOrNull()

                when {
                    previousDefault == null -> Unit
                    overrideDefault -> previousDefault.defaultCurrency = false
                    else -> return@newSuspendedTransaction CurrencyCreateResult.DEFAULT_ALREADY_EXISTS
                }
            }

            val newCurrency = FallbackCurrency.new {
                name = currency.name
                displayName = currency.displayName
                symbol = currency.symbol
                symbolDisplay = currency.symbolDisplay
                scale = currency.scale
                this.defaultCurrency = currency.defaultCurrency
                minimumAmount = currency.minimumAmount
            }

            _fallbackCurrencies.add(newCurrency)
            _currencies.add(newCurrency.toCurrency())

            if (newCurrency.defaultCurrency) {
                _defaultCurrency = newCurrency.toCurrency()
                _currencies.forEach { it.defaultCurrency = it.name.equals(newCurrency.name, true) }
            }

            return@newSuspendedTransaction CurrencyCreateResult.SUCCESS
        }

    private fun validate(c: CoreCurrency): CurrencyCreateResult? {
        fun invalidName() = c.name.isBlank() || c.name.length > CURRENCY_NAME_MAX_LENGTH
        fun invalidSymbol() = c.symbol.isBlank() || c.symbol.length > CURRENCY_SYMBOL_MAX_LENGTH

        if (invalidName()) return CurrencyCreateResult.INVALID_NAME
        if (invalidSymbol()) return CurrencyCreateResult.INVALID_SYMBOL

        val plainName = PlainTextComponentSerializer.plainText().serialize(c.displayName)
        val plainSymbol = PlainTextComponentSerializer.plainText().serialize(c.symbolDisplay)
        if (plainName.isBlank() || plainName.length > CURRENCY_NAME_MAX_LENGTH) return CurrencyCreateResult.INVALID_NAME
        if (plainSymbol.isBlank() || plainSymbol.length > CURRENCY_SYMBOL_MAX_LENGTH) return CurrencyCreateResult.INVALID_SYMBOL
        return null
    }

    override fun getCurrencyByName(name: String) =
        currencies.find { it.name.equals(name, ignoreCase = true) }

    companion object {
        val log = logger()
    }
}