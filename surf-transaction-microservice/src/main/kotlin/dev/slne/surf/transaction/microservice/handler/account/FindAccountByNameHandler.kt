package dev.slne.surf.transaction.microservice.handler.account

import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import dev.slne.surf.transaction.core.common.protocol.account.OptionalAccountResponse
import dev.slne.surf.transaction.core.common.protocol.account.findByAccountName.FindAccountByAccountNameRequestPacket
import dev.slne.surf.transaction.microservice.db.account.AccountRepository
import kotlinx.coroutines.launch

object FindAccountByNameHandler {

    @RabbitHandler
    fun handleFindAccountByAccountName(request: FindAccountByAccountNameRequestPacket) {
        request.launch {
            val account = AccountRepository.findAccountByName(request.accountName)
            request.respond(OptionalAccountResponse(account))
        }
    }
}