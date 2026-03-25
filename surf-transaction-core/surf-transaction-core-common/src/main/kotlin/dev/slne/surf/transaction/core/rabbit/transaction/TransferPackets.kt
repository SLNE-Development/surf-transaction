package dev.slne.surf.transaction.core.rabbit.transaction

import dev.slne.surf.rabbitmq.packet.RabbitRequestPacket
import dev.slne.surf.rabbitmq.packet.RabbitResponsePacket
import dev.slne.surf.surfapi.core.api.serializer.java.number.bigdecimal.SerializableBigDecimal
import dev.slne.surf.surfapi.core.api.serializer.java.uuid.SerializableStringUUID
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import kotlinx.serialization.Serializable

@Serializable
class TransferRequest(
    val initiator: SerializableStringUUID,
    val senderAccountId: SerializableStringUUID,
    val amount: SerializableBigDecimal,
    val currencyName: String,
    val receiverAccountId: SerializableStringUUID,
    val ignoreSenderMinimum: Boolean,
    val ignoreReceiverMinimum: Boolean,
    val additionalSenderData: Set<TransactionData>,
    val additionalReceiverData: Set<TransactionData>
) : RabbitRequestPacket<TransferResponse>()

@Serializable
class TransferResponse(val result: TransactionResultPacket) : RabbitResponsePacket()
