package dev.slne.surf.transaction.api.currency

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.transaction.api.util.InternalTransactionApi

/**
 * Internal service for managing and resolving currencies.
 *
 * [CurrencyService] provides access to all registered currencies and the
 * system-wide default currency. It is used internally by the transaction
 * module to resolve and validate currency instances.
 *
 * This API is strictly internal to the Surf Transaction module and must not
 * be used by external consumers.
 */
@InternalTransactionApi
interface CurrencyService {
    /**
     * The default currency of the system.
     */
    val defaultCurrency: Currency

    /**
     * All currencies registered in the system.
     */
    val currencies: Set<Currency>

    /**
     * Returns a currency by its technical [name], or `null` if none exists.
     *
     * @param name the technical name of the currency
     * @return the resolved currency or `null`
     */
    fun getCurrencyByName(name: String): Currency?

    companion object {
        val instance = requiredService<CurrencyService>()
    }
}