package dev.slne.surf.transaction.core.common.protocol.account.create

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.transaction.core.common.protocol.account.AccountResponse
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class CreateAccountRequestPacket(
    val ownerId: @Contextual UUID,
    val name: String,
    val defaultAccount: Boolean
) : RabbitRequestPacket<AccountResponse>()
