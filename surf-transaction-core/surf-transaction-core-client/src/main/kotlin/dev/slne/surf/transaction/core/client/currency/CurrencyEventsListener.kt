package dev.slne.surf.transaction.core.client.currency

import dev.slne.surf.redis.event.OnRedisEvent
import dev.slne.surf.transaction.core.client.redis.events.currency.ChangedDefaultCurrencyEvent
import dev.slne.surf.transaction.core.client.redis.events.currency.CurrencyCreatedEvent
import dev.slne.surf.transaction.core.client.redis.events.currency.CurrencyEnabledChangedEvent

class CurrencyEventsListener {

    @OnRedisEvent
    fun onCurrencyCreated(event: CurrencyCreatedEvent) {
        CurrencyServiceImpl.INSTANCE.cacheCurrency(event.currency)
    }

    @OnRedisEvent
    fun onCurrencyEnabledChanged(event: CurrencyEnabledChangedEvent) {
        CurrencyServiceImpl.INSTANCE.updateCurrencyEnabled(event.currencyName, event.enabled)
    }

    @OnRedisEvent
    fun onChangedDefaultCurrency(event: ChangedDefaultCurrencyEvent) {
        CurrencyServiceImpl.INSTANCE.updateDefaultCurrency(event.newDefault)
    }
}