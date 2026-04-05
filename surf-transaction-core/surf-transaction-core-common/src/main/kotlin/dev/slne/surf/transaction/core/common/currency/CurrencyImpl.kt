package dev.slne.surf.transaction.core.common.currency

import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.currency.CurrencyScale
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import java.math.BigDecimal

@Serializable
data class CurrencyImpl(
    override val name: String,
    override val displayName: @Contextual Component,
    override val symbol: String,
    override val symbolDisplay: @Contextual Component,
    override val scale: CurrencyScale,
    override val minimumAmount: @Contextual BigDecimal = BigDecimal.ZERO,
    override var defaultCurrency: Boolean = false
) : Currency {
    override fun format(amount: BigDecimal, color: TextColor): Component {
        return buildText {
            text(scale.formatString(amount), color)
            appendSpace()
            append(symbolDisplay)
        }
    }

    companion object {
        val DEFAULT = CurrencyImpl(
            name = "castcoin",
            displayName = buildText { primary("CastCoin") },
            symbol = "CC",
            symbolDisplay = buildText { primary("CC") },
            scale = CurrencyScale.INTEGER,
            defaultCurrency = true,
            minimumAmount = BigDecimal.ZERO
        )
    }
}