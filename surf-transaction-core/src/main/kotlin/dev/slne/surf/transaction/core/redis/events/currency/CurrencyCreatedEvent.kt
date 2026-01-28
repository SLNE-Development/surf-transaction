package dev.slne.surf.transaction.core.redis.events.currency

import dev.slne.surf.redis.event.RedisEvent
import dev.slne.surf.transaction.core.currency.CurrencyImpl
import kotlinx.serialization.Serializable

@Serializable
class CurrencyCreatedEvent(
    val currency: CurrencyImpl
) : RedisEvent()