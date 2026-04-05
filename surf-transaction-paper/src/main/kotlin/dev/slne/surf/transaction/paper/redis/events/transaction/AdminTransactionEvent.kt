package dev.slne.surf.transaction.paper.redis.events.transaction

import dev.slne.surf.api.core.serializer.java.uuid.SerializableUUID
import dev.slne.surf.redis.event.RedisEvent
import dev.slne.surf.transaction.core.client.redis.serializer.NetworkCurrency
import kotlinx.serialization.Serializable

@Serializable
class AdminTransactionEvent(
    val receiverUuid: SerializableUUID,
    val currency: NetworkCurrency,
    val amount: Double,
    val senderName: String,
    val added: Boolean
): RedisEvent()