package dev.slne.surf.transaction.microservice.handler.account

import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import dev.slne.surf.transaction.core.common.protocol.account.findAllByOwner.FindAllAccountsByOwnerRequestPacket
import dev.slne.surf.transaction.core.common.protocol.account.findAllByOwner.FindAllAccountsByOwnerResponsePacket
import dev.slne.surf.transaction.microservice.db.account.AccountRepository
import kotlinx.coroutines.launch

object FindAllAccountByOwnerHandler {

    @RabbitHandler
    fun handleFindAllAccountsByOwner(request: FindAllAccountsByOwnerRequestPacket) {
        request.launch {
            val accounts = AccountRepository.findAccountsByOwner(request.ownerUUID)
            request.respond(FindAllAccountsByOwnerResponsePacket(accounts))
        }
    }
}