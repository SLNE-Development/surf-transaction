package dev.slne.surf.transaction.core.currency

import dev.slne.surf.transaction.api.currency.CurrencyService
import dev.slne.surf.transaction.api.util.InternalTransactionApi

@InternalTransactionApi
interface CoreCurrencyService : CurrencyService {
    suspend fun cacheCurrencies()
    fun cacheCurrency(currency: CurrencyImpl)
    fun updateDefaultCurrency(newDefaultName: String)
    suspend fun createCurrency(currency: CurrencyImpl): CurrencyCreateResult
    suspend fun makeDefaultCurrency(currency: CurrencyImpl): CurrencyDefaultResult

    companion object {
        @OptIn(InternalTransactionApi::class)
        fun get() = CurrencyService.instance as CoreCurrencyService
    }
}
