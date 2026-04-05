package dev.slne.surf.transaction.core.client.currency

import com.google.auto.service.AutoService
import dev.slne.surf.surfapi.core.api.messages.adventure.plain
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.surfapi.core.api.util.toObjectSet
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.currency.CurrencyService
import dev.slne.surf.transaction.core.client.rabbitApi
import dev.slne.surf.transaction.core.client.redis.RedisService
import dev.slne.surf.transaction.core.client.redis.events.currency.ChangedDefaultCurrencyEvent
import dev.slne.surf.transaction.core.client.redis.events.currency.CurrencyCreatedEvent
import dev.slne.surf.transaction.core.common.currency.CoreCurrencyService
import dev.slne.surf.transaction.core.common.currency.CurrencyCreateResult
import dev.slne.surf.transaction.core.common.currency.CurrencyDefaultResult
import dev.slne.surf.transaction.core.common.currency.CurrencyImpl
import dev.slne.surf.transaction.core.common.protocol.currency.create.CreateCurrencyRequestPacket
import dev.slne.surf.transaction.core.common.protocol.currency.findAllAndCreateDefaultCurrencyIfMissing.FindAllCurrenciesAndCreateDefaultCurrencyIfMissingRequestPacket
import dev.slne.surf.transaction.core.common.protocol.currency.makeDefaultCurrency.MakeDefaultCurrencyRequestPacket
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlin.properties.Delegates

@AutoService(CurrencyService::class)
class CurrencyServiceImpl : CoreCurrencyService {
    override var defaultCurrency: CurrencyImpl by Delegates.notNull()
    override var currencies: Set<CurrencyImpl> by Delegates.notNull()

    private val currencyCacheChannel = Channel<Unit>(Channel.CONFLATED)

    private val scope =
        CoroutineScope(Dispatchers.Default + SupervisorJob() + CoroutineName("surf-transaction-currency-service") + CoroutineExceptionHandler { context, throwable ->
            log.atSevere()
                .withCause(throwable)
                .log("Unhandled exception in ${context[CoroutineName]}")
        })

    init {
        scope.launch {
            currencyCacheChannel.consumeEach {
                
                cacheCurrencies0()
            }
        }
    }

    fun disposeScope() {
        scope.cancel("Disposing CurrencyServiceImpl scope")
    }

    override fun getCurrencyByName(name: String) =
        currencies.find { it.name.equals(name, ignoreCase = true) }

    fun cacheCurrencies() {
        currencyCacheChannel.trySend(Unit)
    }

    private suspend fun cacheCurrencies0() {
        val request = FindAllCurrenciesAndCreateDefaultCurrencyIfMissingRequestPacket()
        val (currencies) = rabbitApi.sendRequest(request)

        this.defaultCurrency = currencies.single { it.defaultCurrency }
        this.currencies = currencies.toObjectSet()
    }

    fun cacheCurrency(currency: CurrencyImpl) {
        this.currencies = this.currencies.plus(currency).toObjectSet()
    }

    fun updateDefaultCurrency(newDefaultName: String) {
        val newDefaultCurrency = getCurrencyByName(newDefaultName) ?: return

        this.defaultCurrency.defaultCurrency = false
        newDefaultCurrency.defaultCurrency = true

        defaultCurrency = newDefaultCurrency
    }

    override suspend fun createCurrency(currency: CurrencyImpl): CurrencyCreateResult {
        require(!currency.defaultCurrency) { "Cannot create default currency. Use makeDefaultCurrency() instead." }
        validateCurrency(currency)?.let { return it }

        val request = CreateCurrencyRequestPacket(currency)
        val (result) = rabbitApi.sendRequest(request)

        if (result == CurrencyCreateResult.SUCCESS) {
            RedisService.publish(CurrencyCreatedEvent(currency)).await()
        }

        return result
    }

    private fun validateCurrency(c: CurrencyImpl): CurrencyCreateResult? {
        fun invalidName() = c.name.isBlank() || c.name.length > Currency.CURRENCY_NAME_MAX_LENGTH
        fun invalidSymbol() =
            c.symbol.isBlank() || c.symbol.length > Currency.CURRENCY_SYMBOL_MAX_LENGTH

        if (invalidName()) return CurrencyCreateResult.INVALID_NAME
        if (invalidSymbol()) return CurrencyCreateResult.INVALID_SYMBOL

        val plainName = c.displayName.plain()
        val plainSymbol = c.symbolDisplay.plain()

        if (plainName.isBlank() || plainName.length > Currency.CURRENCY_NAME_MAX_LENGTH) return CurrencyCreateResult.INVALID_NAME
        if (plainSymbol.isBlank() || plainSymbol.length > Currency.CURRENCY_SYMBOL_MAX_LENGTH) return CurrencyCreateResult.INVALID_SYMBOL

        return null
    }

    override suspend fun makeDefaultCurrency(currency: CurrencyImpl): CurrencyDefaultResult {
        require(!currency.defaultCurrency) { "Currency '${currency.name}' is already the default currency." }

        val request = MakeDefaultCurrencyRequestPacket(currency.name)
        val (result) = rabbitApi.sendRequest(request)

        if (result == CurrencyDefaultResult.SUCCESS) {
            RedisService.publish(ChangedDefaultCurrencyEvent(currency.name)).await()
        }

        return result
    }

    companion object {
        private val log = logger()

        fun get() = CurrencyService.instance as CurrencyServiceImpl
    }
}