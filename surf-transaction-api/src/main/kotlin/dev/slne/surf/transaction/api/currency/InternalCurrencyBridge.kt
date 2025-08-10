package dev.slne.surf.transaction.api.currency

import dev.slne.surf.transaction.api.InternalTransactionApiBridge
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import it.unimi.dsi.fastutil.objects.ObjectSet
import org.springframework.beans.factory.getBean

@InternalTransactionApi
interface InternalCurrencyBridge {
    /**
     * Retrieves the default currency used in the transaction system.
     */
    val defaultCurrency: Currency

    /**
     * All available currencies in the transaction system.
     */
    val currencies: ObjectSet<out Currency>

    /**
     * Retrieves a currency by its unique name.
     *
     * @param name The unique identifier of the currency to retrieve.
     * @return The currency associated with the given name, or null if no such currency exists.
     */
    fun getCurrencyByName(name: String): Currency? =
        currencies.find { it.name.equals(name, ignoreCase = true) }

    companion object {
        val instance get() = InternalTransactionApiBridge.instance.context.getBean<InternalCurrencyBridge>()
    }
}