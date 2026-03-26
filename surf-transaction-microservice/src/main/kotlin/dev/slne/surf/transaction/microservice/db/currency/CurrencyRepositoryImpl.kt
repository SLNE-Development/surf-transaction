package dev.slne.surf.transaction.microservice.db.currency

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ResultRow
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.statements.UpdateBuilder
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.*
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.database.utils.asDataIntegrityViolation
import dev.slne.surf.transaction.core.common.currency.CurrencyCreateResult
import dev.slne.surf.transaction.core.common.currency.CurrencyDefaultResult
import dev.slne.surf.transaction.core.common.currency.CurrencyImpl
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList


class CurrencyRepositoryImpl : CurrencyRepository {
    override fun findCurrencyIDByNameQuery(name: String): Query = CurrencyTable.select(CurrencyTable.id)
        .where { CurrencyTable.name eq name }
        .limit(1)

    override suspend fun findAllAndCreateDefaultCurrencyIfMissing() = suspendTransaction {
        val defaultExists = CurrencyTable
            .select(CurrencyTable.id)
            .where { CurrencyTable.defaultCurrency eq true }
            .limit(1)
            .singleOrNull() != null

        if (!defaultExists) {
            CurrencyTable.insertIgnore {
                insertCurrency(CurrencyImpl.DEFAULT, it)
            }
        }

        CurrencyTable
            .selectAll()
            .map(::fromResultRow)
            .toList()
    }

    override suspend fun createCurrency(currency: CurrencyImpl): CurrencyCreateResult = suspendTransaction {
        try {
            CurrencyTable.insert {
                insertCurrency(currency, it)
            }
            CurrencyCreateResult.SUCCESS
        } catch (e: ExposedR2dbcException) {
            e.asDataIntegrityViolation()
            CurrencyCreateResult.ALREADY_EXISTS
        }
    }

    override suspend fun makeDefaultCurrency(currencyName: String): CurrencyDefaultResult = suspendTransaction {
        val targetRow = CurrencyTable
            .select(CurrencyTable.id, CurrencyTable.defaultCurrency)
            .where { CurrencyTable.name eq currencyName }
            .limit(1)
            .singleOrNull()
            ?: return@suspendTransaction CurrencyDefaultResult.NOT_FOUND

        if (targetRow[CurrencyTable.defaultCurrency]) {
            return@suspendTransaction CurrencyDefaultResult.SUCCESS
        }

        CurrencyTable.update({ CurrencyTable.defaultCurrency eq true }) {
            it[defaultCurrency] = false
        }

        CurrencyTable.update({ CurrencyTable.id eq targetRow[CurrencyTable.id] }) {
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
        minimumAmount = row[CurrencyTable.minimumAmount]
    )

    private fun CurrencyTable.insertCurrency(currency: CurrencyImpl, stm: UpdateBuilder<*>) {
        stm[name] = currency.name
        stm[symbol] = currency.symbol
        stm[displayName] = currency.displayName
        stm[symbolDisplay] = currency.symbolDisplay
        stm[scale] = currency.scale
        stm[defaultCurrency] = currency.defaultCurrency
        stm[minimumAmount] = currency.minimumAmount
    }
}