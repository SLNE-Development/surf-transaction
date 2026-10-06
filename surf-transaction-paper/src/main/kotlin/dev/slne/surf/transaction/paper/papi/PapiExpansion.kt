package dev.slne.surf.transaction.paper.papi

import dev.slne.surf.api.paper.hook.papi.expansion.PapiExpansion
import dev.slne.surf.transaction.paper.papi.placeholder.CurrencyDisplayPlaceholder
import dev.slne.surf.transaction.paper.papi.placeholder.CurrentBalancePlaceholder

object PapiExpansion : PapiExpansion(
    "surf-transaction", listOf(
        CurrentBalancePlaceholder,
        CurrencyDisplayPlaceholder
    ), "red"
)