package dev.slne.surf.transaction.server.currency.db

import dev.slne.surf.transaction.core.currency.CurrencyImpl
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class CurrencyEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<CurrencyEntity>(CurrencyTable)

    var name by CurrencyTable.name
    var displayName by CurrencyTable.displayName
    var symbol by CurrencyTable.symbol
    var symbolDisplay by CurrencyTable.symbolDisplay
    var scale by CurrencyTable.scale
    var defaultCurrency by CurrencyTable.defaultCurrency
    var minimumAmount by CurrencyTable.minimumAmount

    fun toApi() = CurrencyImpl(
        name = name,
        displayName = displayName,
        symbol = symbol,
        symbolDisplay = symbolDisplay,
        scale = scale,
        defaultCurrency = defaultCurrency,
        minimumAmount = minimumAmount
    )
}