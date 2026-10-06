package dev.slne.surf.transaction.core.client.balance

import dev.slne.surf.redis.event.OnRedisEvent
import dev.slne.surf.transaction.core.client.redis.events.balance.BalanceChangedEvent

class BalanceEventsListener {

    @OnRedisEvent
    fun onBalanceChanged(event: BalanceChangedEvent) {
        if (event.originatesFromThisClient()) {
            return
        }

        BalanceCache.refreshAccounts(event.accountIds, event.currencyName)
    }
}
