package dev.slne.surf.transaction.core.common.db.currency

import dev.slne.surf.transaction.core.common.currency.CurrencyCreateResult
import dev.slne.surf.transaction.core.common.currency.CurrencyDefaultResult
import dev.slne.surf.transaction.core.common.currency.CurrencyImpl

interface CurrencyRepository {

    suspend fun findAllAndCreateDefaultCurrencyIfMissing(): List<CurrencyImpl>
    suspend fun findCurrencyIDByName(name: String): ULong?

    suspend fun createCurrency(currency: CurrencyImpl): CurrencyCreateResult
    suspend fun makeDefaultCurrency(currency: CurrencyImpl): CurrencyDefaultResult

    companion object : CurrencyRepository by CurrencyRepositoryImpl() {
        fun init() = Unit
    }
}