package dev.slne.surf.transaction.core.common.protocol.account.findByAccountId

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.transaction.core.common.protocol.account.OptionalAccountResponse
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class FindAccountByAccountIdRequestPacket(val accountId: @Contextual UUID) :
    RabbitRequestPacket<OptionalAccountResponse>()