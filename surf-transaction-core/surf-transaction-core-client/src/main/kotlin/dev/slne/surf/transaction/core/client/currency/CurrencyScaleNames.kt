package dev.slne.surf.transaction.core.client.currency

import dev.slne.surf.transaction.api.currency.CurrencyScale

/**
 * The literal names under which the [CurrencyScale] entries are offered on the command line.
 */
object CurrencyScaleNames {

    /**
     * Every scale name, in declaration order.
     */
    val all: List<String> = CurrencyScale.entries.map { it.name.lowercase() }

    /**
     * Resolves the scale [name] was offered for.
     *
     * @throws IllegalArgumentException if [name] is not one of [all]
     */
    fun parse(name: String): CurrencyScale = CurrencyScale.valueOf(name.uppercase())
}
