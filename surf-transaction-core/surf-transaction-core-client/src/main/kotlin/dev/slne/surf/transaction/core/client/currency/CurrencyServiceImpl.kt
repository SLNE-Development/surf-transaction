package dev.slne.surf.transaction.core.client.currency

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.messages.adventure.plain
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.api.core.util.toObjectSet
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
import dev.slne.surf.transaction.core.common.protocol.currency.findAllOrCreateDefault.FindAllCurrenciesAndCreateDefaultCurrencyIfMissingRequestPacket
import dev.slne.surf.transaction.core.common.protocol.currency.makeDefaultCurrency.MakeDefaultCurrencyRequestPacket
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import java.util.concurrent.atomic.AtomicReference

@AutoService(CurrencyService::class)
class CurrencyServiceImpl : CoreCurrencyService {
    private val registry = AtomicReference<CurrencyRegistry>()

    private val currencyCacheChannel = Channel<Unit>(Channel.CONFLATED)

    private val scope =
        CoroutineScope(Dispatchers.Default + SupervisorJob() + CoroutineName("surf-transaction-currency-service") + CoroutineExceptionHandler { context, throwable ->
            log.atSevere()
                .withCause(throwable)
                .log("Unhandled exception in ${context[CoroutineName]}")
        })

    init {
        scope.launch {
            currencyCacheChannel.consumeEach { loadCurrencies() }
        }
    }

    override val defaultCurrency: CurrencyImpl get() = loadedRegistry().defaultCurrency
    override val currencies: Set<CurrencyImpl> get() = loadedRegistry().currencies

    fun disposeScope() {
        scope.cancel("Disposing CurrencyServiceImpl scope")
    }

    override fun getCurrencyByName(name: String) = loadedRegistry().byName(name)

    suspend fun loadCurrencies() {
        try {
            val request = FindAllCurrenciesAndCreateDefaultCurrencyIfMissingRequestPacket()
            val (currencies) = rabbitApi.sendRequest(request)

            registry.set(
                CurrencyRegistry(currencies, currencies.single { it.defaultCurrency })
            )
        } catch (cause: CancellationException) {
            throw cause
        } catch (cause: Throwable) {
            log.atSevere().withCause(cause).log("Failed to load the currency cache")
        }
    }

    /** Requests an asynchronous registry refresh; repeated requests are coalesced. */
    fun cacheCurrencies() {
        currencyCacheChannel.trySend(Unit)
    }

    fun cacheCurrency(currency: CurrencyImpl) {
        loadedRegistry()
        registry.updateAndGet { current -> current.plus(currency) }
    }

    fun updateDefaultCurrency(newDefaultName: String) {
        loadedRegistry()
        registry.updateAndGet { current -> current.withDefault(newDefaultName) ?: current }
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

    private fun loadedRegistry(): CurrencyRegistry = registry.get()
        ?: error("Currencies have not been cached yet")

    companion object {
        private val log = logger()

        val INSTANCE get() = CurrencyService.INSTANCE as CurrencyServiceImpl
    }
}

internal class CurrencyRegistry(
    currencies: List<CurrencyImpl>,
    val defaultCurrency: CurrencyImpl
) {
    private val ordered: List<CurrencyImpl> = currencies.toList()
    val currencies: Set<CurrencyImpl> = ordered.toObjectSet()

    fun byName(name: String): CurrencyImpl? {
        val index = indexOfName(name)
        return if (index >= 0) ordered[index] else null
    }

    fun plus(currency: CurrencyImpl): CurrencyRegistry {
        if (currency in currencies) return this

        return CurrencyRegistry(ordered + currency, defaultCurrency)
    }

    fun withDefault(name: String): CurrencyRegistry? {
        val targetIndex = indexOfName(name)
        if (targetIndex < 0) return null
        if (ordered[targetIndex] === defaultCurrency) return this

        val updated = ObjectArrayList<CurrencyImpl>(ordered.size)
        for (index in ordered.indices) {
            val currency = ordered[index]
            updated += when {
                index == targetIndex -> currency.copy(defaultCurrency = true)
                currency.defaultCurrency -> currency.copy(defaultCurrency = false)
                else -> currency
            }
        }

        return CurrencyRegistry(updated, updated[targetIndex])
    }

    private fun indexOfName(name: String): Int {
        for (index in ordered.indices) {
            if (ordered[index].name.equals(name, ignoreCase = true)) return index
        }

        return -1
    }
}
