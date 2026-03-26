package dev.slne.surf.transaction.microservice.handler.account

import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import dev.slne.surf.rabbitmq.api.packet.standard.response.primitive.PrimitiveResponse
import dev.slne.surf.transaction.core.common.protocol.account.existsByAccountName.ExistsAccountByAccountNameRequestPacket
import dev.slne.surf.transaction.microservice.db.account.AccountRepository
import kotlinx.coroutines.launch

object ExistsAccountByAccountNameHandler {

    @RabbitHandler
    fun handleExistsAccountByAccountName(request: ExistsAccountByAccountNameRequestPacket) {
        request.launch {
            val exists = AccountRepository.existsByAccountName(request.name)
            request.respond(PrimitiveResponse.BooleanResponsePacket(exists))
        }
    }
}