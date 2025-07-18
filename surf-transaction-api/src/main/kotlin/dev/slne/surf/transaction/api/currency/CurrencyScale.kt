package dev.slne.surf.transaction.api.currency

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.logging.Formatter

enum class CurrencyScale {

    /**
     * No decimal places
     */
    INTEGER {
        override fun format(amount: BigDecimal): BigDecimal {
            return amount.setScale(0)
        }
    },

    /**
     * Two decimal places
     */
    DECIMAL_2 {
        override fun format(amount: BigDecimal): BigDecimal {
            return amount.setScale(2)
        }
    };


    /**
     * Formats the given amount to the scale of the currency
     *
     * @param amount The amount to format
     *
     * @return The formatted amount
     */
    abstract fun format(amount: BigDecimal): BigDecimal


    /**
     * Formats the given amount to the scale of the currency
     *
     * @param amount The amount to format
     *
     * @return The formatted amount
     */
    fun format(amount: Double): BigDecimal = format(amount.toBigDecimal())

    /**
     * Formats the given amount as a string representation using the number formatting
     * rules of the currency's scale.
     *
     * @param amount The amount to format as a string.
     * @return The formatted string representation of the amount.
     */
    fun formatString(amount: BigDecimal): String = NumberFormat.getNumberInstance().format(format(amount))

    /**
     * Formats the given amount to a string representation.
     *
     * @param amount The amount as a double to be formatted.
     * @return The formatted string representation of the amount.
     */
    fun formatString(amount: Double): String = formatString(amount.toBigDecimal())
}