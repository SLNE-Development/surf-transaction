package dev.slne.surf.transaction.core.currency

import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.currency.CurrencyScale
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import java.math.BigDecimal

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
    override val defaultCurrency: Boolean,
    override val minimumAmount: BigDecimal = BigDecimal.ZERO
) : Currency {

    override fun format(amount: BigDecimal, color: TextColor): Component {
        return buildText {
            append(Component.text("${scale.format(amount)}", color))
            append(Component.text(" "))
            append(symbolDisplay)
        }
    }

    override fun toString(): String {
        return "CoreCurrency(name='$name', displayName=$displayName, symbol='$symbol', symbolDisplay=$symbolDisplay, scale=$scale, defaultCurrency=$defaultCurrency, minimumAmount=$minimumAmount)"
    }
}