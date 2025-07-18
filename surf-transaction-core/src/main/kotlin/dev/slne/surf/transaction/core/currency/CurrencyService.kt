package dev.slne.surf.transaction.core.currency

import dev.slne.surf.surfapi.core.api.util.requiredService
import dev.slne.surf.transaction.api.currency.Currency
import it.unimi.dsi.fastutil.objects.ObjectSet
import org.jetbrains.annotations.UnmodifiableView

/**
 * A service that provides access to currencies.
 */
interface CurrencyService {

    /**
     * Fetches all currencies from the database into memory.
     *
     * @return a list of all currencies
     */
    suspend fun fetchCurrencies(): ObjectSet<out Currency>


    /**
     * Returns a list of all currencies
     *
     * @return a list of all currencies
     */
    val currencies: @UnmodifiableView ObjectSet<out Currency>

    val defaultCurrency: Currency

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
    suspend fun createCurrency(currency: CoreCurrency): CurrencyCreateResult

    /**
     * Sets the specified currency as the default currency in the system.
     *
     * <b>This method will also create the currency in the database
     * if it does not already exist.</b>
     *
     * @param currency The currency to set as the default.
     * @return A [CurrencyCreateResult] indicating the result of the operation.
     */
    suspend fun makeDefaultCurrency(currency: CoreCurrency): CurrencyCreateResult

    /**
     * Returns a currency from memory
     *
     * @param name the name of the currency
     *
     * @return the currency or null if not found
     */
    operator fun get(name: String): Currency? = getCurrencyByName(name)

    companion object : CurrencyService by INSTANCE {
        val instance = INSTANCE
    }
}

private val INSTANCE = requiredService<CurrencyService>()