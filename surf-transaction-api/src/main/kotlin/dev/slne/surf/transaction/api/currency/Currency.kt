package dev.slne.surf.transaction.api.currency

import dev.slne.surf.surfapi.core.api.messages.Colors
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import java.math.BigDecimal

interface Currency {

    /**
     * The name / id of the currency
     * e.g. castcoin
     */
    val name: String

    /**
     * The display name of the currency
     * e.g. <red>CastCoin</red>
     */
    val displayName: Component

    /**
     * The symbol of the currency
     * e.g. $
     */
    val symbol: String

    /**
     * The display symbol of the currency
     * e.g. <red>$</red>
     */
    val symbolDisplay: Component

    /**
     * The scale of the currency
     * e.g. 2.00 or 2
     */
    val scale: CurrencyScale

    /**
     * If the currency is the default currency
     */
    val defaultCurrency: Boolean

    /**
     * The minimum amount of the currency
     */
    val minimumAmount: BigDecimal

    /**
     * Formats the amount to a component
     *
     * @param amount The amount to format
     * @param color The color of the component
     *
     * @return The formatted component
     */
    fun format(amount: BigDecimal, color: TextColor = Colors.VARIABLE_VALUE): Component

    /**
     * Formats the amount to a component
     *
     * @param amount The amount to format
     * @param color The color of the component
     *
     * @return The formatted component
     */
    fun format(amount: Double, color: TextColor = Colors.VARIABLE_VALUE) =
        format(BigDecimal.valueOf(amount), color)

}