package dev.slne.surf.transaction.core.currency

import dev.slne.surf.surfapi.core.api.util.requiredService
import dev.slne.surf.transaction.api.currency.Currency
import it.unimi.dsi.fastutil.objects.ObjectSet

/**
 * A service that provides access to currencies.
 */
interface CurrencyService {

    /**
     * Fetches all currencies from the database into memory.
     *
     * @return a list of all currencies
     */
    suspend fun fetchCurrencies(): ObjectSet<Currency>

    /**
     * Returns a list of all currencies
     *
     * @return a list of all currencies
     */
    val currencies: ObjectSet<Currency>

    /**
     * Returns a currency from memory
     *
     * @param name the name of the currency
     *
     * @return the currency or null if not found
     */
    fun getCurrencyByName(name: String): Currency?

    /**
     * Creates a currency
     *
     * @param currency the currency to create
     *
     * @return the created currency
     */
    suspend fun createCurrency(currency: Currency): CurrencyCreateResult

    /**
     * Returns a currency from memory
     *
     * @param name the name of the currency
     *
     * @return the currency or null if not found
     */
    operator fun get(name: String): Currency? = getCurrencyByName(name)

    companion object {
        /**
         * The instance of the currency service
         */
        val INSTANCE = requiredService<CurrencyService>()
    }
}

/**
 * The instance of the currency service
 */
val currencyService get() = CurrencyService.INSTANCE