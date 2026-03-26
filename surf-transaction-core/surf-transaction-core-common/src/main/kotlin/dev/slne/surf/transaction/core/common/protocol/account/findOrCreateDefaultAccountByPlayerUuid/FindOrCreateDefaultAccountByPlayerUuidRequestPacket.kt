package dev.slne.surf.transaction.core.common.protocol.account.findOrCreateDefaultAccountByPlayerUuid

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import dev.slne.surf.transaction.core.common.protocol.account.AccountResponse
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class FindOrCreateDefaultAccountByPlayerUuidRequestPacket(val playerUuid: @Contextual UUID) :
    RabbitRequestPacket<AccountResponse>()