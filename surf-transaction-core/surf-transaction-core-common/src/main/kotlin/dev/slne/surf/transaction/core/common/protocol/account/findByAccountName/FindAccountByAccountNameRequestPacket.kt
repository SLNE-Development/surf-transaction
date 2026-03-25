package dev.slne.surf.transaction.core.common.protocol.account.findByAccountName

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.transaction.core.common.protocol.account.OptionalAccountResponse
import kotlinx.serialization.Serializable

@Serializable
data class FindAccountByAccountNameRequestPacket(val accountName: String) :
    RabbitRequestPacket<OptionalAccountResponse>()
