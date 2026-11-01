package dev.slne.surf.transaction.microservice.db.currency

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.Query
import dev.slne.surf.transaction.core.common.currency.CurrencyCreateResult
import dev.slne.surf.transaction.core.common.currency.CurrencyDefaultResult
import dev.slne.surf.transaction.core.common.currency.CurrencyEnabledResult
import dev.slne.surf.transaction.core.common.currency.CurrencyImpl

interface CurrencyRepository {

    fun findCurrencyIDByNameQuery(name: String): Query

    suspend fun findAllAndCreateDefaultCurrencyIfMissing(): List<CurrencyImpl>

    suspend fun createCurrency(currency: CurrencyImpl): CurrencyCreateResult
    suspend fun makeDefaultCurrency(currencyName: String): CurrencyDefaultResult
    suspend fun setCurrencyEnabled(currencyName: String, enabled: Boolean): CurrencyEnabledResult

    /**
     * Returns whether the currency [currencyName] is enabled, or `null` if it does not exist.
     */
    suspend fun isCurrencyEnabled(currencyName: String): Boolean?

    companion object : CurrencyRepository by CurrencyRepositoryImpl() {
        fun init() = Unit
    }
}