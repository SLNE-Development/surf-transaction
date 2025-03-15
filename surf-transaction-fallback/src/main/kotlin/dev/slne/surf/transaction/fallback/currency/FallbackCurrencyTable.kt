package dev.slne.surf.transaction.fallback.currency

import dev.slne.surf.transaction.api.currency.CurrencyScale
import dev.slne.surf.transaction.core.currency.CURRENCY_NAME_MAX_LENGTH
import dev.slne.surf.transaction.core.currency.CURRENCY_SYMBOL_MAX_LENGTH
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.jetbrains.exposed.dao.id.LongIdTable

object FallbackCurrencyTable : LongIdTable("transaction_currencies") {

    val name = varchar("name", CURRENCY_NAME_MAX_LENGTH)
    val displayName = largeText("display_name").transform(
        { GsonComponentSerializer.gson().deserialize(it) },
        { GsonComponentSerializer.gson().serialize(it) })
    val symbol = varchar("symbol", CURRENCY_SYMBOL_MAX_LENGTH)
    val symbolDisplay = largeText("symbol_display").transform(
        { GsonComponentSerializer.gson().deserialize(it) },
        { GsonComponentSerializer.gson().serialize(it) })
    val scale = enumeration("scale", CurrencyScale::class)
    val defaultCurrency = bool("default_currency")

}