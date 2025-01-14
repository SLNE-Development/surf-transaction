package dev.slne.transaction.api.currency

import net.kyori.adventure.util.Services

/**
 * A service that provides access to currencies.
 */
interface CurrencyService {

    /**
     * Fetches all currencies from the database into memory.
     *
     * @return a list of all currencies
     */
    suspend fun fetchCurrencies(): List<Currency>

    /**
     * Returns a currency from memory
     *
     * @param name the name of the currency
     *
     * @return the currency or null if not found
     */
    fun getCurrency(name: String): Currency?

    /**
     * Returns a currency from memory
     *
     * @param id the id of the currency
     *
     * @return the currency or null if not found
     */
    fun getCurrencyById(id: Long): Currency?

    /**
     * Returns a currency from memory
     *
     * @param name the name of the currency
     *
     * @return the currency or null if not found
     */
    operator fun get(name: String): Currency? = getCurrency(name)

    companion object {
        val INSTANCE: CurrencyService = Services.serviceWithFallback(CurrencyService::class.java).orElseThrow {
            IllegalStateException("No CurrencyService implementation found")
        }
    }
}

val currencyService: CurrencyService
    get() = CurrencyService.INSTANCE