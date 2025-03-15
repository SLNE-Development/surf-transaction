package dev.slne.surf.transaction.api.currency

import net.kyori.adventure.text.Component
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

}