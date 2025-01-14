package dev.slne.transaction.api.currency

import net.kyori.adventure.text.Component

interface Currency {

    val name: String
    val displayName: Component

    val symbol: String
    val symbolDisplay: Component

    val scale: CurrencyScale

    val defaultCurrency: Boolean

}