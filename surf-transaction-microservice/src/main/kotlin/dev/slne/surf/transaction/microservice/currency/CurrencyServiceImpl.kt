package dev.slne.surf.transaction.microservice.currency

import com.google.auto.service.AutoService
import dev.slne.surf.surfapi.core.api.util.toObjectSet
import dev.slne.surf.transaction.api.currency.Currency.Companion.CURRENCY_NAME_MAX_LENGTH
import dev.slne.surf.transaction.api.currency.Currency.Companion.CURRENCY_SYMBOL_MAX_LENGTH
import dev.slne.surf.transaction.api.currency.CurrencyService
import dev.slne.surf.transaction.core.currency.CoreCurrencyService
import dev.slne.surf.transaction.core.currency.CurrencyCreateResult
import dev.slne.surf.transaction.core.currency.CurrencyDefaultResult
import dev.slne.surf.transaction.core.currency.CurrencyImpl
import dev.slne.surf.transaction.core.db.currency.CurrencyRepository
import dev.slne.surf.transaction.core.redis.RedisService
import dev.slne.surf.transaction.core.redis.events.currency.ChangedDefaultCurrencyEvent
import dev.slne.surf.transaction.core.redis.events.currency.CurrencyCreatedEvent
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import kotlin.properties.Delegates

@AutoService(CurrencyService::class)
class CurrencyServiceImpl : CoreCurrencyService {
    override var defaultCurrency: CurrencyImpl by Delegates.notNull()
    override var currencies: Set<CurrencyImpl> by Delegates.notNull()

    override fun getCurrencyByName(name: String) =
        currencies.find { it.name.equals(name, ignoreCase = true) }

    override suspend fun cacheCurrencies() {
        val currencies = CurrencyRepository.findAllAndCreateDefaultCurrencyIfMissing()
        defaultCurrency = currencies.single { it.defaultCurrency }
        this.currencies = currencies.toObjectSet()
    }

    override fun cacheCurrency(currency: CurrencyImpl) {
        currencies = currencies.plus(currency).toObjectSet()
    }

    override fun updateDefaultCurrency(newDefaultName: String) {
        val newDefaultCurrency =
            currencies.find { it.name.equals(newDefaultName, ignoreCase = true) } ?: return
        defaultCurrency.defaultCurrency = false
        newDefaultCurrency.defaultCurrency = true
        defaultCurrency = newDefaultCurrency
    }

    override suspend fun createCurrency(currency: CurrencyImpl): CurrencyCreateResult {
        require(!currency.defaultCurrency) {
            "Cannot create default currency. Use makeDefaultCurrency() instead."
        }
        validateCurrency(currency)?.let { return it }

        val result = CurrencyRepository.createCurrency(currency)

        if (result == CurrencyCreateResult.SUCCESS) {
            RedisService.publish(CurrencyCreatedEvent(currency)).await()
        }

        return result
    }

    private fun validateCurrency(c: CurrencyImpl): CurrencyCreateResult? {
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

    override suspend fun makeDefaultCurrency(currency: CurrencyImpl): CurrencyDefaultResult {
        require(!currency.defaultCurrency) {
            "Currency '${currency.name}' is already the default currency."
        }

        val result = CurrencyRepository.makeDefaultCurrency(currency)

        if (result == CurrencyDefaultResult.SUCCESS) {
            RedisService.publish(ChangedDefaultCurrencyEvent(currency.name)).await()
        }

        return result
    }

    companion object {
        fun get() = CurrencyService.instance as CurrencyServiceImpl
    }
}
