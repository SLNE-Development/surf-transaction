package dev.slne.surf.transaction.microservice.db.currency

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.Query
import dev.slne.surf.transaction.core.common.currency.CurrencyCreateResult
import dev.slne.surf.transaction.core.common.currency.CurrencyDefaultResult
import dev.slne.surf.transaction.core.common.currency.CurrencyImpl

interface CurrencyRepository {

    fun findCurrencyIDByNameQuery(name: String): Query

    suspend fun findAllAndCreateDefaultCurrencyIfMissing(): List<CurrencyImpl>

    suspend fun createCurrency(currency: CurrencyImpl): CurrencyCreateResult
    suspend fun makeDefaultCurrency(currency: CurrencyImpl): CurrencyDefaultResult

    companion object : CurrencyRepository by CurrencyRepositoryImpl() {
        fun init() = Unit
    }
}