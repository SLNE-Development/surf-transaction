package dev.slne.surf.transaction.paper.server.redis.events.pay

import dev.slne.surf.redis.event.RedisEvent
import dev.slne.surf.surfapi.core.api.serializer.java.uuid.SerializableUUID
import dev.slne.surf.transaction.core.redis.serializer.NetworkCurrency
import kotlinx.serialization.Serializable

@Serializable
class PaymentReceivedEvent(
    val receiverUUID: SerializableUUID,
    val senderName: String,
    val currency: NetworkCurrency,
    val amount: Double
): RedisEvent()