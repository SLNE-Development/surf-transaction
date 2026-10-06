package dev.slne.surf.transaction.paper.papi.placeholder

import dev.slne.surf.api.paper.hook.papi.expansion.PapiPlaceholder
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.core.client.balance.BalanceCache
import org.bukkit.OfflinePlayer

object CurrentBalancePlaceholder : PapiPlaceholder("current-balance") {
    override fun parse(
        player: OfflinePlayer,
        args: List<String>
    ): String? {
        val currencyName = args.getOrNull(0) ?: return null
        val currency = Currency.byName(currencyName) ?: return null

        val balance = BalanceCache.cachedBalance(player.uniqueId, currency) ?: return "..."

        return balance.toPlainString()
    }
}
