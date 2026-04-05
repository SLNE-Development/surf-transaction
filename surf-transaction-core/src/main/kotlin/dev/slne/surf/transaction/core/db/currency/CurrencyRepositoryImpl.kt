package dev.slne.surf.transaction.core.db.currency

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ResultRow
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.statements.InsertStatement
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.*
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.transaction.core.currency.CurrencyCreateResult
import dev.slne.surf.transaction.core.currency.CurrencyDefaultResult
import dev.slne.surf.transaction.core.currency.CurrencyImpl
import dev.slne.surf.transaction.core.redis.RedisService
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import kotlin.time.Duration.Companion.hours


class CurrencyRepositoryImpl : CurrencyRepository {
    private val currencyIDByNameCache =
        RedisService.cache<String, ULong>("currency_id_by_name", 5.hours)

    override suspend fun findAllAndCreateDefaultCurrencyIfMissing() = suspendTransaction {
        val currencies = CurrencyTable.selectAll().map(::fromResultRow).toList()

        if (currencies.any { it.defaultCurrency }) {
            currencies
        } else {
            val defaultCurrency = CurrencyTable.insertReturning {
                insertCurrency(CurrencyImpl.DEFAULT, it)
            }.map(::fromResultRow).single()

            currencies + defaultCurrency
        }
    }

    override suspend fun findCurrencyIDByName(name: String): ULong? {
        return currencyIDByNameCache.cachedOrLoadNullable(name) {
            CurrencyTable.select(CurrencyTable.id)
                .where { CurrencyTable.name eq name }
                .singleOrNull()
                ?.get(CurrencyTable.id)?.value
        }
    }

    override suspend fun createCurrency(
        currency: CurrencyImpl
    ): CurrencyCreateResult = suspendTransaction {
        val exists = findCurrencyIDByName(currency.name) != null

        if (exists) {
            return@suspendTransaction CurrencyCreateResult.ALREADY_EXISTS
        }

        CurrencyTable.insert {
            insertCurrency(currency, it)
        }

        CurrencyCreateResult.SUCCESS
    }

    override suspend fun makeDefaultCurrency(
        currency: CurrencyImpl
    ): CurrencyDefaultResult = suspendTransaction {
        val newDefaultCurrencyID = findCurrencyIDByName(currency.name)
            ?: return@suspendTransaction CurrencyDefaultResult.NOT_FOUND

        CurrencyTable.update({ CurrencyTable.defaultCurrency eq true }) {
            it[defaultCurrency] = false
        }

        CurrencyTable.update({ CurrencyTable.id eq newDefaultCurrencyID }) {
            it[defaultCurrency] = true
        }

        CurrencyDefaultResult.SUCCESS
    }.also { result ->
        if (result == CurrencyDefaultResult.SUCCESS) {
            currencyIDByNameCache.invalidate(currency.name)
        }
    }

    private fun fromResultRow(row: ResultRow) = CurrencyImpl(
        name = row[CurrencyTable.name],
        symbol = row[CurrencyTable.symbol],
        displayName = row[CurrencyTable.displayName],
        symbolDisplay = row[CurrencyTable.symbolDisplay],
        scale = row[CurrencyTable.scale],
        defaultCurrency = row[CurrencyTable.defaultCurrency],
        minimumAmount = row[CurrencyTable.minimumAmount]
    )

    private fun CurrencyTable.insertCurrency(currency: CurrencyImpl, stm: InsertStatement<Number>) {
        stm[name] = currency.name
        stm[symbol] = currency.symbol
        stm[displayName] = currency.displayName
        stm[symbolDisplay] = currency.symbolDisplay
        stm[scale] = currency.scale
        stm[defaultCurrency] = currency.defaultCurrency
        stm[minimumAmount] = currency.minimumAmount
    }
}