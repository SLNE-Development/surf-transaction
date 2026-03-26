package dev.slne.surf.transaction.microservice.handler.account

import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import dev.slne.surf.transaction.core.common.protocol.account.OptionalAccountResponse
import dev.slne.surf.transaction.core.common.protocol.account.findByAccountId.FindAccountByAccountIdRequestPacket
import dev.slne.surf.transaction.microservice.db.account.AccountRepository
import kotlinx.coroutines.launch

object FindAccountByAccountIdHandler {

    @RabbitHandler
    fun handleFindAccountByAccountId(request: FindAccountByAccountIdRequestPacket) {
        request.launch {
            val account = AccountRepository.findAccountByAccountId(request.accountId)
            request.respond(OptionalAccountResponse(account))
        }
    }
}