package dev.slne.surf.transaction.paper.redis.events.pay

import dev.slne.surf.redis.event.RedisEvent
import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.transaction.core.client.redis.serializer.NetworkCurrency
import kotlinx.serialization.Serializable

/**
 * Published when a player received money from another player.
 *
 * Redis identifies events by their fully qualified class name, so this package is part of the wire
 * format and must stay stable across every server that exchanges this event.
 */
@Serializable
class PaymentReceivedEvent(
    val receiverUUID: SerializableUUID,
    val senderName: String,
    val currency: NetworkCurrency,
    val amount: Double
): RedisEvent()
