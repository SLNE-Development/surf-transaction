package dev.slne.surf.transaction.api

import dev.slne.surf.surfapi.core.api.util.requiredService
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.user.TransactionUser
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.util.*

interface TransactionApi {

    /**
     * Get the default currency
     *
     * @return The default currency
     */
    fun getDefaultCurrency(): Currency?

    /**
     * Get the currencies available in memory
     *
     * @return The currencies available in memory
     */
    fun getCurrencies(): ObjectSet<Currency>

    /**
     * Returns a currency from memory
     *
     * @param name the name of the currency
     *
     * @return the currency or null if not found
     */
    fun getCurrencyByName(name: String): Currency?

    /**
     * Get a user by their UUID
     *
     * @param uuid The UUID of the user
     *
     * @return The user with the UUID
     */
    fun getTransactionUser(uuid: UUID): TransactionUser

    companion object {
        /**
         * The instance of the TransactionApi
         */
        val INSTANCE = requiredService<TransactionApi>()
    }

}

/**
 * The instance of the TransactionApi
 */
val transactionApi get() = TransactionApi.INSTANCE