package dev.slne.surf.transaction.core.db.currency

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.dao.id.EntityID
import dev.slne.surf.transaction.core.currency.CurrencyCreateResult
import dev.slne.surf.transaction.core.currency.CurrencyDefaultResult
import dev.slne.surf.transaction.core.currency.CurrencyImpl

interface CurrencyRepository {

    suspend fun findAllAndCreateDefaultCurrencyIfMissing(): List<CurrencyImpl>
    suspend fun findCurrencyIDByName(name: String): EntityID<Long>?

    suspend fun createCurrency(currency: CurrencyImpl): CurrencyCreateResult
    suspend fun makeDefaultCurrency(currency: CurrencyImpl): CurrencyDefaultResult

    companion object : CurrencyRepository by CurrencyRepositoryImpl()
}