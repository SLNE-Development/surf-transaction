package dev.slne.surf.transaction.core.client.currency

import dev.slne.surf.transaction.api.currency.CurrencyScale
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class CurrencyScaleNamesTest {

    @Test
    fun `all names are lowercase and cover every scale`() {
        assertEquals(CurrencyScale.entries.size, CurrencyScaleNames.all.size)
        assertEquals(CurrencyScaleNames.all, CurrencyScaleNames.all.map { it.lowercase() })
    }

    @Test
    fun `every offered name parses back to its scale`() {
        CurrencyScale.entries.forEachIndexed { index, scale ->
            assertEquals(scale, CurrencyScaleNames.parse(CurrencyScaleNames.all[index]))
        }
    }

    @Test
    fun `parsing is case insensitive`() {
        assertEquals(CurrencyScale.DECIMAL_2, CurrencyScaleNames.parse("Decimal_2"))
        assertEquals(CurrencyScale.INTEGER, CurrencyScaleNames.parse("INTEGER"))
    }

    @Test
    fun `an unknown name is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            CurrencyScaleNames.parse("decimal_3")
        }
    }
}
