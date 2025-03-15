package dev.slne.surf.transaction.api.currency

import java.math.BigDecimal

enum class CurrencyScale {

    /**
     * No decimal places
     */
    INTEGER,

    /**
     * Two decimal places
     */
    DECIMAL_2;

    /**
     * Formats the given amount to the scale of the currency
     *
     * @param amount The amount to format
     *
     * @return The formatted amount
     */
    fun format(amount: BigDecimal): BigDecimal {
        return when (this) {
            INTEGER -> amount.setScale(0)
            DECIMAL_2 -> amount.setScale(2)
        }
    }

    /**
     * Formats the given amount to the scale of the currency
     *
     * @param amount The amount to format
     *
     * @return The formatted amount
     */
    fun format(amount: Double): BigDecimal {
        return when (this) {
            INTEGER -> BigDecimal.valueOf(amount).setScale(0)
            DECIMAL_2 -> BigDecimal.valueOf(amount).setScale(2)
        }
    }
}