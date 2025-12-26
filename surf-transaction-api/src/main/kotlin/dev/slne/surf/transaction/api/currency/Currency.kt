package dev.slne.surf.transaction.api.currency

import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.TextColor
import java.math.BigDecimal

@OptIn(InternalTransactionApi::class)
interface Currency : ComponentLike {
    val name: String
    val displayName: Component
    val defaultCurrency: Boolean
    val symbol: String
    val symbolDisplay: Component
    val scale: CurrencyScale
    val minimumAmount: BigDecimal

    fun format(amount: BigDecimal, color: TextColor = Colors.VARIABLE_VALUE): Component

    fun format(amount: Double, color: TextColor = Colors.VARIABLE_VALUE) =
        format(amount.toBigDecimal(), color)

    override fun asComponent(): Component = displayName

    companion object {
        const val CURRENCY_NAME_MAX_LENGTH = 16
        const val CURRENCY_SYMBOL_MAX_LENGTH = 16

        fun default(): Currency = CurrencyService.instance.defaultCurrency
        fun all(): Set<Currency> = CurrencyService.instance.currencies
        fun byName(name: String): Currency? = CurrencyService.instance.getCurrencyByName(name)
        operator fun get(name: String): Currency? = byName(name)
    }
}