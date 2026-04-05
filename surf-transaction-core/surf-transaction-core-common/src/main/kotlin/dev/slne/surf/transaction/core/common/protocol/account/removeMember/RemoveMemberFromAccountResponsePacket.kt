package dev.slne.surf.transaction.core.common.protocol.account.removeMember

import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import dev.slne.surf.transaction.api.account.member.results.AccountMemberResult
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class RemoveMemberFromAccountResponsePacket(val result: AccountMemberResult, val accountOwnerUuid: @Contextual UUID?) :
    RabbitResponsePacket()
