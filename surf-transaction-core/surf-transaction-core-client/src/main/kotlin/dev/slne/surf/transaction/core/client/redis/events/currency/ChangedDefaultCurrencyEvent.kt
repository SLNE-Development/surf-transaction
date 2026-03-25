package dev.slne.surf.transaction.core.client.redis.events.currency

import dev.slne.surf.redis.event.RedisEvent
import kotlinx.serialization.Serializable

@Serializable
class ChangedDefaultCurrencyEvent(val newDefault: String) : RedisEvent()