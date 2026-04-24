package dev.slne.surf.transaction.api.currency

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*

/**
 * Defines how monetary values of a currency are scaled and formatted.
 *
 * A [CurrencyScale] controls the number of fractional digits used for a currency
 * and how values are rounded before being displayed or processed.
 */
enum class CurrencyScale {

    /**
     * Integer-based currency scale.
     *
     * Values are rounded to whole numbers using [RoundingMode.HALF_UP].
     */
    INTEGER {
        override fun format(amount: BigDecimal): BigDecimal {
            return amount.setScale(0, RoundingMode.HALF_UP)
        }
    },

    /**
     * Decimal currency scale with two fractional digits.
     *
     * Values are rounded to two decimal places using [RoundingMode.HALF_UP].
     */
    DECIMAL_2 {
        override fun format(amount: BigDecimal): BigDecimal {
            return amount.setScale(2, RoundingMode.HALF_UP)
        }
    };

    /**
     * Formats the given [amount] according to this scale.
     *
     * This method applies the appropriate scale and rounding mode
     * for the currency.
     *
     * @param amount the monetary value to format
     * @return the scaled and rounded value
     */
    abstract fun format(amount: BigDecimal): BigDecimal

    /**
     * Formats the given [amount] as a localized string.
     *
     * The value is first scaled using [format] and then converted into
     * a locale-aware string representation.
     *
     * @param amount the monetary value to format
     * @param locale the locale used for number formatting
     * @return a localized string representation of the amount
     */
    fun formatString(
        amount: BigDecimal,
        locale: Locale = Locale.getDefault(Locale.Category.FORMAT)
    ): String = NumberFormat.getNumberInstance(locale).format(format(amount))

    companion object {
        /**
         * The maximum allowed transaction amount based on database constraints.
         *
         * The database column is defined as DECIMAL(20,10), which supports
         * values up to 9999999999.9999999999.
         */
        val MAX_VALUE: BigDecimal = BigDecimal("9999999999.9999999999")

        /**
         * Returns the maximum allowed value for this currency scale.
         *
         * @return the maximum BigDecimal value that can be represented with this scale
         */
        fun CurrencyScale.maxValue(): BigDecimal =
            MAX_VALUE.setScale(
                when (this) {
                    INTEGER -> 0
                    DECIMAL_2 -> 2
                },
                RoundingMode.DOWN
            )
    }
}