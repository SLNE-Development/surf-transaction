package dev.slne.surf.transaction.api.currency

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*

enum class CurrencyScale {
    INTEGER {
        override fun format(amount: BigDecimal): BigDecimal {
            return amount.setScale(0, RoundingMode.HALF_UP)
        }
    },

    DECIMAL_2 {
        override fun format(amount: BigDecimal): BigDecimal {
            return amount.setScale(2, RoundingMode.HALF_UP)
        }
    };

    abstract fun format(amount: BigDecimal): BigDecimal

    fun formatString(
        amount: BigDecimal,
        locale: Locale = Locale.getDefault(Locale.Category.FORMAT)
    ): String = NumberFormat.getNumberInstance(locale).format(format(amount))
}