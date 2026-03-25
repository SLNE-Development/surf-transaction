package dev.slne.surf.transaction.core.common.currency

import dev.slne.surf.redis.event.OnRedisEvent
import dev.slne.surf.transaction.core.common.redis.events.currency.ChangedDefaultCurrencyEvent
import dev.slne.surf.transaction.core.common.redis.events.currency.CurrencyCreatedEvent

class CurrencyEventsListener {

    @OnRedisEvent
    fun onCurrencyCreated(event: CurrencyCreatedEvent) {
        CurrencyServiceImpl.get().cacheCurrency(event.currency)
    }

    @OnRedisEvent
    fun onChangedDefaultCurrency(event: ChangedDefaultCurrencyEvent) {
        CurrencyServiceImpl.get().updateDefaultCurrency(event.newDefault)
    }
}