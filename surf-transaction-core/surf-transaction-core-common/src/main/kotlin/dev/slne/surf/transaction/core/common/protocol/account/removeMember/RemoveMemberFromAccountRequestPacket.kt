package dev.slne.surf.transaction.core.common.protocol.account.removeMember

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class RemoveMemberFromAccountRequestPacket(
    val accountId: @Contextual UUID,
    val executor: @Contextual UUID,
    val target: @Contextual UUID
) : RabbitRequestPacket<RemoveMemberFromAccountResponsePacket>()
