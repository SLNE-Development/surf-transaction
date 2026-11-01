package dev.slne.surf.transaction.microservice.db.currency

import dev.slne.surf.database.columns.component
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.or
import dev.slne.surf.database.table.AuditableLongIdTable
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.currency.CurrencyScale

object CurrencyTable: AuditableLongIdTable("currencies") {
    val name = varchar("name", Currency.CURRENCY_NAME_MAX_LENGTH).uniqueIndex()
    val displayName = component("display_name")
    val symbol = varchar("symbol", Currency.CURRENCY_SYMBOL_MAX_LENGTH)
    val symbolDisplay = component("symbol_display")

    val scale = enumerationByName<CurrencyScale>("scale", 255)
    val defaultCurrency = bool("default_currency")
    val minimumAmount = decimal("minimum_amount", 20, 10)
    val enabled = bool("enabled").default(true)

    init {
        check("currencies_default_must_be_enabled") {
            (enabled eq true) or (defaultCurrency eq false)
        }
    }
}