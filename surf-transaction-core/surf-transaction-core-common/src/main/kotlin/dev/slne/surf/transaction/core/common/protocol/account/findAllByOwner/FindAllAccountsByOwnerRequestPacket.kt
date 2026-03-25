package dev.slne.surf.transaction.core.common.protocol.account.findAllByOwner

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class FindAllAccountsByOwnerRequestPacket(val ownerUUID: @Contextual UUID) :
    RabbitRequestPacket<FindAllAccountsByOwnerResponsePacket>()