package dev.slne.surf.transaction.core.rabbit.account

import dev.slne.surf.rabbitmq.packet.RabbitRequestPacket
import dev.slne.surf.rabbitmq.packet.RabbitResponsePacket
import dev.slne.surf.surfapi.core.api.serializer.java.uuid.SerializableStringUUID
import dev.slne.surf.transaction.core.account.AccountImpl
import kotlinx.serialization.Serializable

@Serializable
class GetAllAccountsByOwnerRequest(val ownerUuid: SerializableStringUUID) :
    RabbitRequestPacket<GetAllAccountsByOwnerResponse>()

@Serializable
class GetAllAccountsByOwnerResponse(val accounts: List<AccountImpl>) : RabbitResponsePacket()
