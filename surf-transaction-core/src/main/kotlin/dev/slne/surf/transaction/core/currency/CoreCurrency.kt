package dev.slne.surf.transaction.core.currency

import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.currency.CurrencyScale
import net.kyori.adventure.text.Component

const val CURRENCY_NAME_MAX_LENGTH = 16
const val CURRENCY_SYMBOL_MAX_LENGTH = 16

/**
 * A core implementation of [Currency].
 */
class CoreCurrency(
    override val name: String,
    override val displayName: Component,
    override val symbol: String,
    override val symbolDisplay: Component,
    override val scale: CurrencyScale,
    override val defaultCurrency: Boolean
) : Currency