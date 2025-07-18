package dev.slne.surf.transaction.api

import dev.slne.surf.surfapi.core.api.util.requiredService
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.user.TransactionUser
import it.unimi.dsi.fastutil.objects.ObjectSet
import org.jetbrains.annotations.UnmodifiableView
import java.util.*

interface TransactionApi {

    /**
     * Represents the default currency used in transactions.
     *
     * This currency is utilized as the primary or standard currency when
     * performing financial operations within the application. It is expected
     * to align with the default settings configured for the transaction system.
     */
    val defaultCurrency: Currency

    /**
     * Represents the set of available currencies within the transaction system.
     *
     * This property provides a read-only view of the supported currencies that can be used
     * for financial transactions. Each currency in this set includes metadata such as its
     * name, display name, symbol, and other relevant attributes. The set is unmodifiable,
     * ensuring that the available currencies cannot be altered at runtime.
     *
     * @see Currency for details about the properties of each currency
     * @see TransactionApi.getCurrencies for dynamically retrieving the available currencies
     */
    val currencies: @UnmodifiableView ObjectSet<out Currency>

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

    companion object : TransactionApi by requiredService<TransactionApi>()
}