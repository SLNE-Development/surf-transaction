package dev.slne.surf.transaction.paper.papi.placeholder

import dev.slne.surf.api.paper.hook.papi.expansion.PapiPlaceholder
import dev.slne.surf.transaction.api.currency.Currency
import org.bukkit.OfflinePlayer

object CurrencyDisplayPlaceholder : PapiPlaceholder("currency-display") {
    override fun parse(
        player: OfflinePlayer,
        args: List<String>
    ): String? {
        val currencyName = args.getOrNull(0) ?: return null
        val currency = Currency.byName(currencyName) ?: return null

        return currency.symbol
    }
}
