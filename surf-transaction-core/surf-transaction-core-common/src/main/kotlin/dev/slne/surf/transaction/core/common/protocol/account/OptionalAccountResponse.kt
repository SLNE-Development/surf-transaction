package dev.slne.surf.transaction.core.common.protocol.account

import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import dev.slne.surf.transaction.core.common.account.AccountImpl
import kotlinx.serialization.Serializable

@Serializable
data class OptionalAccountResponse(val account: AccountImpl?) : RabbitResponsePacket()