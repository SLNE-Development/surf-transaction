package dev.slne.surf.transaction.fallback.currency

import dev.slne.surf.transaction.api.currency.CurrencyScale
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import org.jetbrains.exposed.dao.id.LongIdTable

object FallbackCurrencyTable : LongIdTable("transaction_currencies") {

    val name = varchar("name", 255)
    val displayName = largeText("display_name").transform(
        { GsonComponentSerializer.gson().deserialize(it) },
        { GsonComponentSerializer.gson().serialize(it) })
    val symbol = varchar("symbol", 255)
    val symbolDisplay = largeText("symbol_display").transform(
        { GsonComponentSerializer.gson().deserialize(it) },
        { GsonComponentSerializer.gson().serialize(it) })
    val scale = enumeration("scale", CurrencyScale::class)
    val defaultCurrency = bool("default_currency")

}