package dev.slne.surf.transaction.core.rabbit.account

import dev.slne.surf.rabbitmq.packet.RabbitRequestPacket
import dev.slne.surf.rabbitmq.packet.RabbitResponsePacket
import dev.slne.surf.surfapi.core.api.serializer.java.uuid.SerializableStringUUID
import dev.slne.surf.transaction.core.account.AccountImpl
import kotlinx.serialization.Serializable

@Serializable
class GetDefaultAccountRequest(val playerUuid: SerializableStringUUID) :
    RabbitRequestPacket<GetDefaultAccountResponse>()

@Serializable
class GetDefaultAccountResponse(val account: AccountImpl) : RabbitResponsePacket()
