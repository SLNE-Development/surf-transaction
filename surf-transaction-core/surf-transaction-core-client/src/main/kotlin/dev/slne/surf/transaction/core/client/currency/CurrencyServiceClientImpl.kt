package dev.slne.surf.transaction.core.client.currency

import com.google.auto.service.AutoService
import dev.slne.surf.microservice.api.rabbit.client.ClientRabbitMQApi
import dev.slne.surf.surfapi.core.api.util.requiredService
import dev.slne.surf.surfapi.core.api.util.toObjectSet
import dev.slne.surf.transaction.api.currency.CurrencyService
import dev.slne.surf.transaction.core.currency.CoreCurrencyService
import dev.slne.surf.transaction.core.currency.CurrencyCreateResult
import dev.slne.surf.transaction.core.currency.CurrencyDefaultResult
import dev.slne.surf.transaction.core.currency.CurrencyImpl
import dev.slne.surf.transaction.core.rabbit.currency.CreateCurrencyRequest
import dev.slne.surf.transaction.core.rabbit.currency.GetAllCurrenciesRequest
import dev.slne.surf.transaction.core.rabbit.currency.MakeDefaultCurrencyRequest
import kotlin.properties.Delegates

@AutoService(CurrencyService::class)
class CurrencyServiceClientImpl : CoreCurrencyService {
    override var defaultCurrency: CurrencyImpl by Delegates.notNull()
    override var currencies: Set<CurrencyImpl> by Delegates.notNull()

    private val rabbitApi get() = requiredService<ClientRabbitMQApi>()

    override fun getCurrencyByName(name: String) =
        currencies.find { it.name.equals(name, ignoreCase = true) }

    override suspend fun cacheCurrencies() {
        val response = rabbitApi.sendRequest(GetAllCurrenciesRequest())
        currencies = response.currencies.toObjectSet()
        defaultCurrency = response.currencies.find { it.name == response.defaultCurrencyName }
            ?: response.currencies.firstOrNull()
            ?: CurrencyImpl.DEFAULT
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
        val response = rabbitApi.sendRequest(CreateCurrencyRequest(currency))
        return response.result
    }

    override suspend fun makeDefaultCurrency(currency: CurrencyImpl): CurrencyDefaultResult {
        val response = rabbitApi.sendRequest(MakeDefaultCurrencyRequest(currency.name))
        return response.result
    }
}
