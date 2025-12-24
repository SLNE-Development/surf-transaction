package dev.slne.surf.transaction.core.db.currency

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ResultRow
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.dao.id.EntityID
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.statements.InsertStatement
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insert
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insertReturning
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.select
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.selectAll
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.update
import dev.slne.surf.transaction.core.currency.CurrencyCreateResult
import dev.slne.surf.transaction.core.currency.CurrencyDefaultResult
import dev.slne.surf.transaction.core.currency.CurrencyImpl
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList


class CurrencyRepositoryImpl : CurrencyRepository {
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

    override suspend fun findCurrencyIDByName(name: String): EntityID<Long>? {
        return CurrencyTable.select(CurrencyTable.id)
            .where { CurrencyTable.name eq name }
            .singleOrNull()
            ?.get(CurrencyTable.id)
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
    }

    private fun fromResultRow(row: ResultRow) = CurrencyImpl(
        name = row[CurrencyTable.name],
        symbol = row[CurrencyTable.symbol],
        displayName = row[CurrencyTable.displayName],
        symbolDisplay = row[CurrencyTable.symbolDisplay],
        scale = row[CurrencyTable.scale],
        defaultCurrency = row[CurrencyTable.defaultCurrency],
    )

    private fun CurrencyTable.insertCurrency(currency: CurrencyImpl, stm: InsertStatement<Number>) {
        stm[name] = currency.name
        stm[symbol] = currency.symbol
        stm[displayName] = currency.displayName
        stm[symbolDisplay] = currency.symbolDisplay
        stm[scale] = currency.scale
        stm[defaultCurrency] = currency.defaultCurrency
    }
}