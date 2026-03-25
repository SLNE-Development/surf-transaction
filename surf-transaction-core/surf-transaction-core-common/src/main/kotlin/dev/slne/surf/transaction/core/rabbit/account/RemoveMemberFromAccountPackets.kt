package dev.slne.surf.transaction.core.rabbit.account

import dev.slne.surf.rabbitmq.packet.RabbitRequestPacket
import dev.slne.surf.rabbitmq.packet.RabbitResponsePacket
import dev.slne.surf.surfapi.core.api.serializer.java.uuid.SerializableStringUUID
import dev.slne.surf.transaction.api.account.member.results.AccountMemberResult
import kotlinx.serialization.Serializable

@Serializable
class RemoveMemberFromAccountRequest(
    val accountId: SerializableStringUUID,
    val executor: SerializableStringUUID,
    val target: SerializableStringUUID
) : RabbitRequestPacket<RemoveMemberFromAccountResponse>()

@Serializable
class RemoveMemberFromAccountResponse(val result: AccountMemberResult) : RabbitResponsePacket()
