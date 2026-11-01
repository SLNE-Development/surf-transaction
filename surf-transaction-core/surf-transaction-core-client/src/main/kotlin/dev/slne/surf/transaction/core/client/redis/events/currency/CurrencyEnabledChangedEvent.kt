package dev.slne.surf.transaction.core.client.redis.events.currency

import dev.slne.surf.redis.event.RedisEvent
import kotlinx.serialization.Serializable

@Serializable
class CurrencyEnabledChangedEvent(val currencyName: String, val enabled: Boolean) : RedisEvent()
