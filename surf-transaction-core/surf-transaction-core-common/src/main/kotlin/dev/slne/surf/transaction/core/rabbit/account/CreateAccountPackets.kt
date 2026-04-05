package dev.slne.surf.transaction.core.rabbit.account

import dev.slne.surf.rabbitmq.packet.RabbitRequestPacket
import dev.slne.surf.rabbitmq.packet.RabbitResponsePacket
import dev.slne.surf.surfapi.core.api.serializer.java.uuid.SerializableStringUUID
import dev.slne.surf.transaction.api.account.result.AccountCreationResult
import dev.slne.surf.transaction.core.account.AccountImpl
import kotlinx.serialization.Serializable

@Serializable
class CreateAccountRequest(val ownerUuid: SerializableStringUUID, val name: String) :
    RabbitRequestPacket<CreateAccountResponse>()

@Serializable
class CreateAccountResponse(
    val account: AccountImpl?,
    val failureReason: AccountCreationResult.FailureReason?
) : RabbitResponsePacket()
