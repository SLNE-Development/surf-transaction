package dev.slne.surf.transaction.microservice.handler.account

import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import dev.slne.surf.rabbitmq.api.packet.standard.response.primitive.PrimitiveResponse
import dev.slne.surf.transaction.core.common.protocol.account.delete.DeleteAccountRequestPacket
import dev.slne.surf.transaction.microservice.db.account.AccountRepository
import kotlinx.coroutines.launch

object DeleteAccountHandler {

    @RabbitHandler
    fun handleDeleteAccount(request: DeleteAccountRequestPacket) {
        request.launch {
            val deleted = AccountRepository.deleteAccount(request.accountId)
            request.respond(PrimitiveResponse.LongResponsePacket(deleted.toLong()))
        }
    }
}