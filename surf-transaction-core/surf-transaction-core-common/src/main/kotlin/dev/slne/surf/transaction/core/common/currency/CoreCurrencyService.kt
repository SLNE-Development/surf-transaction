package dev.slne.surf.transaction.core.common.currency

import dev.slne.surf.transaction.api.currency.CurrencyService

interface CoreCurrencyService : CurrencyService {

    suspend fun createCurrency(currency: CurrencyImpl): CurrencyCreateResult
    suspend fun makeDefaultCurrency(currency: CurrencyImpl): CurrencyDefaultResult

    companion object : CoreCurrencyService by CurrencyService.INSTANCE as CoreCurrencyService
}