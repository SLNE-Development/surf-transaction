package dev.slne.surf.transaction.api.currency

import dev.slne.surf.surfapi.core.api.util.requiredService
import dev.slne.surf.transaction.api.util.InternalTransactionApi

@InternalTransactionApi
interface CurrencyService {
    val defaultCurrency: Currency
    val currencies: Set<Currency>

    fun getCurrencyByName(name: String): Currency?

    companion object {
        val instance = requiredService<CurrencyService>()
    }
}