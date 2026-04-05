package dev.slne.surf.transaction.core.common.protocol.account.findAllByOwner

import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import dev.slne.surf.transaction.core.common.account.AccountImpl
import kotlinx.serialization.Serializable

@Serializable
data class FindAllAccountsByOwnerResponsePacket(val accounts: Set<AccountImpl>) : RabbitResponsePacket()