package dev.slne.surf.transaction.server.currency.db

import dev.slne.surf.cloud.api.server.exposed.columns.component
import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongIdTable
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.currency.CurrencyScale

object CurrencyTable : AuditableLongIdTable("transaction_currencies") {
    val name = char("name", Currency.CURRENCY_NAME_MAX_LENGTH).uniqueIndex()
    val displayName = component("display_name")
    val symbol = char("symbol", Currency.CURRENCY_SYMBOL_MAX_LENGTH)
    val symbolDisplay = component("symbol_display")

    val scale = enumerationByName<CurrencyScale>("scale", 255)
    val defaultCurrency = bool("default_currency")
    val minimumAmount = decimal("minimum_amount", 20, 10)

}