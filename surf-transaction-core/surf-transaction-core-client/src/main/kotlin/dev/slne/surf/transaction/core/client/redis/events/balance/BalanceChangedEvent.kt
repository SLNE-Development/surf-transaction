package dev.slne.surf.transaction.core.client.redis.events.balance

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.redis.event.RedisEvent
import kotlinx.serialization.Serializable

/**
 * Published after a transaction changed the balance of [accountIds] in the currency [currencyName],
 * so every server re-queries the balances it has cached for those accounts.
 */
@Serializable
class BalanceChangedEvent(
    val accountIds: Set<SerializableUUID>,
    val currencyName: String
) : RedisEvent()
