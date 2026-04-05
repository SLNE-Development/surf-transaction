package dev.slne.surf.transaction.core.common.protocol.account.addMember

import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import dev.slne.surf.transaction.api.account.member.results.AccountMemberResult
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class AddMemberToAccountResponsePacket(val result: AccountMemberResult, val accountOwnerUuid: @Contextual UUID?) :
    RabbitResponsePacket()
