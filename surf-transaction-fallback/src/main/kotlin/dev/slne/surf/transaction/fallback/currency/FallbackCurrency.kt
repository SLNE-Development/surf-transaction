package dev.slne.surf.transaction.fallback.currency

import dev.slne.surf.transaction.core.currency.CoreCurrency
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class FallbackCurrency(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<FallbackCurrency>(FallbackCurrencyTable)

    var name by FallbackCurrencyTable.name
    var displayName by FallbackCurrencyTable.displayName
    var symbol by FallbackCurrencyTable.symbol
    var symbolDisplay by FallbackCurrencyTable.symbolDisplay
    var scale by FallbackCurrencyTable.scale
    var defaultCurrency by FallbackCurrencyTable.defaultCurrency
    var minimumAmount by FallbackCurrencyTable.minimumAmount

    fun toCurrency() = CoreCurrency(
        name,
        displayName,
        symbol,
        symbolDisplay,
        scale,
        defaultCurrency,
        minimumAmount
    )
}