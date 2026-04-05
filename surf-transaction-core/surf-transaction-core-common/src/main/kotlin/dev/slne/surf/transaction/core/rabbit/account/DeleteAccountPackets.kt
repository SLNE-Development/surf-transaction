package dev.slne.surf.transaction.core.rabbit.account

import dev.slne.surf.rabbitmq.packet.RabbitRequestPacket
import dev.slne.surf.rabbitmq.packet.RabbitResponsePacket
import dev.slne.surf.surfapi.core.api.serializer.java.uuid.SerializableStringUUID
import dev.slne.surf.transaction.api.account.result.AccountDeleteResult
import kotlinx.serialization.Serializable

@Serializable
class DeleteAccountRequest(val accountId: SerializableStringUUID) :
    RabbitRequestPacket<DeleteAccountResponse>()

@Serializable
class DeleteAccountResponse(val result: AccountDeleteResult) : RabbitResponsePacket()
