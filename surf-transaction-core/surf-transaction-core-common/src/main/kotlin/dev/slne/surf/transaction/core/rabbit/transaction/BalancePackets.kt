package dev.slne.surf.transaction.core.rabbit.transaction

import dev.slne.surf.rabbitmq.packet.RabbitRequestPacket
import dev.slne.surf.rabbitmq.packet.RabbitResponsePacket
import dev.slne.surf.surfapi.core.api.serializer.java.number.bigdecimal.SerializableBigDecimal
import dev.slne.surf.surfapi.core.api.serializer.java.uuid.SerializableStringUUID
import kotlinx.serialization.Serializable

@Serializable
class BalanceRequest(
    val accountId: SerializableStringUUID,
    val currencyName: String
) : RabbitRequestPacket<BalanceResponse>()

@Serializable
class BalanceResponse(val balance: SerializableBigDecimal) : RabbitResponsePacket()
