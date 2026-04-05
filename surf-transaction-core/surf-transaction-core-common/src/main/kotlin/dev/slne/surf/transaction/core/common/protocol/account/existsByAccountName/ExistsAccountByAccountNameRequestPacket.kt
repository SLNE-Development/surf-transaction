package dev.slne.surf.transaction.core.common.protocol.account.existsByAccountName

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.rabbitmq.api.packet.standard.response.primitive.PrimitiveResponse
import kotlinx.serialization.Serializable

@Serializable
data class ExistsAccountByAccountNameRequestPacket(val name: String) :
    RabbitRequestPacket<PrimitiveResponse.BooleanResponsePacket>()