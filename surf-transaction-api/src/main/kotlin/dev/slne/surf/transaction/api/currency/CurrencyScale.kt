package dev.slne.surf.transaction.api.currency

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*

/**
 * Supported decimal precisions for monetary values.
 *
 * Each entry provides a custom implementation of [format] that clamps a
 * [BigDecimal] to the appropriate scale. Helper overloads are supplied for
 * `Double` conversion and locale-aware string formatting via [NumberFormat].
 */
enum class CurrencyScale {

    /**
     * No fractional digits (`scale = 0`).
     */
    INTEGER {
        override fun format(amount: BigDecimal): BigDecimal {
            return amount.setScale(0, RoundingMode.HALF_UP)
        }
    },

    /**
     * Exactly two fractional digits (`scale = 2`).
     */
    DECIMAL_2 {
        override fun format(amount: BigDecimal): BigDecimal {
            return amount.setScale(2, RoundingMode.HALF_UP)
        }
    };

    /**
     * Returns [amount] rounded to this scale.
     *
     * @param amount value to adjust
     * @return a new [BigDecimal] with the correct scale
     */
    abstract fun format(amount: BigDecimal): BigDecimal

    /**
     * Returns a locale-aware string representation of [amount] after applying
     * this scale’s rounding rules.
     *
     * @param amount  value to convert and scale
     * @param locale  formatting locale; defaults to the JVM’s current
     *                [`FORMAT`](https://docs.oracle.com/javase/8/docs/api/java/util/Locale.Category.html#FORMAT) locale
     * @return        human-readable number, e.g. `"1,234.00"`
     */
    fun formatString(
        amount: BigDecimal,
        locale: Locale = Locale.getDefault(Locale.Category.FORMAT)
    ): String = NumberFormat.getNumberInstance(locale).format(format(amount))
}