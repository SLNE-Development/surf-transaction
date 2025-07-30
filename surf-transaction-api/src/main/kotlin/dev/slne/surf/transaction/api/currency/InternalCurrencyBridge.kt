package dev.slne.surf.transaction.api.currency

import dev.slne.surf.transaction.api.InternalTransactionApiBridge
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import it.unimi.dsi.fastutil.objects.ObjectSet
import org.springframework.beans.factory.getBean

@InternalTransactionApi
interface InternalCurrencyBridge {
    val defaultCurrency: Currency
    val currencies: ObjectSet<out Currency>
    fun getCurrencyByName(name: String): Currency? =
        currencies.find { it.name.equals(name, ignoreCase = true) }

    companion object {
        val instance get() = InternalTransactionApiBridge.instance.context.getBean<InternalCurrencyBridge>()
    }
}