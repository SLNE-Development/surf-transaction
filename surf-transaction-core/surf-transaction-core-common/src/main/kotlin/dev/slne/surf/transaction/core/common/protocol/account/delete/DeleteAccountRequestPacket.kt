package dev.slne.surf.transaction.core.common.protocol.account.delete

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.rabbitmq.api.packet.standard.response.primitive.PrimitiveResponse
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class DeleteAccountRequestPacket(val accountId: @Contextual UUID) :
    RabbitRequestPacket<PrimitiveResponse.LongResponsePacket>()
