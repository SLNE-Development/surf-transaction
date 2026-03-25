package dev.slne.surf.transaction.core.common.protocol.account.addMember

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class AddMemberToAccountRequestPacket(
    val accountId: @Contextual UUID,
    val executor: @Contextual UUID,
    val target: @Contextual UUID
) : RabbitRequestPacket<AddMemberToAccountResponsePacket>()
