package dev.slne.surf.transaction.core.rabbit.account

import dev.slne.surf.rabbitmq.packet.RabbitRequestPacket
import dev.slne.surf.rabbitmq.packet.RabbitResponsePacket
import dev.slne.surf.surfapi.core.api.serializer.java.uuid.SerializableStringUUID
import dev.slne.surf.transaction.core.account.AccountImpl
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
class GetAccountByAccountIdRequest(val accountId: SerializableStringUUID) :
    RabbitRequestPacket<GetAccountByAccountIdResponse>()

@Serializable
class GetAccountByAccountIdResponse(val account: AccountImpl?) : RabbitResponsePacket()
