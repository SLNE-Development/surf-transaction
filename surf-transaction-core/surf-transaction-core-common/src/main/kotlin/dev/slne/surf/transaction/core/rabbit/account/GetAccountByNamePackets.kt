package dev.slne.surf.transaction.core.rabbit.account

import dev.slne.surf.rabbitmq.packet.RabbitRequestPacket
import dev.slne.surf.rabbitmq.packet.RabbitResponsePacket
import dev.slne.surf.transaction.core.account.AccountImpl
import kotlinx.serialization.Serializable

@Serializable
class GetAccountByNameRequest(val name: String) : RabbitRequestPacket<GetAccountByNameResponse>()

@Serializable
class GetAccountByNameResponse(val account: AccountImpl?) : RabbitResponsePacket()
