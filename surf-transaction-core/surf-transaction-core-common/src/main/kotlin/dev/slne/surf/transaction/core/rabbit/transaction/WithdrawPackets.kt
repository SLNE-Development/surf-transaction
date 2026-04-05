package dev.slne.surf.transaction.core.rabbit.transaction

import dev.slne.surf.rabbitmq.packet.RabbitRequestPacket
import dev.slne.surf.rabbitmq.packet.RabbitResponsePacket
import dev.slne.surf.surfapi.core.api.serializer.java.number.bigdecimal.SerializableBigDecimal
import dev.slne.surf.surfapi.core.api.serializer.java.uuid.SerializableStringUUID
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import kotlinx.serialization.Serializable

@Serializable
class WithdrawRequest(
    val accountId: SerializableStringUUID,
    val initiator: SerializableStringUUID,
    val amount: SerializableBigDecimal,
    val currencyName: String,
    val ignoreMinimum: Boolean,
    val additionalData: Set<TransactionData>
) : RabbitRequestPacket<WithdrawResponse>()

@Serializable
class WithdrawResponse(val result: TransactionResultPacket) : RabbitResponsePacket()
