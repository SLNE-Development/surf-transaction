package dev.slne.surf.transaction.core.common.protocol.transaction.balance

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class GetTransactionBalanceRequestPacket(val accountId: @Contextual UUID, val currencyName: String) :
    RabbitRequestPacket<GetTransactionBalanceResponsePacket>()