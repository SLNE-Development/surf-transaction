package dev.slne.surf.transaction.microservice.handler.account

import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import dev.slne.surf.transaction.core.common.protocol.account.AccountResponse
import dev.slne.surf.transaction.core.common.protocol.account.create.CreateAccountRequestPacket
import dev.slne.surf.transaction.microservice.db.account.AccountRepository
import kotlinx.coroutines.launch

object CreateAccountHandler {

    @RabbitHandler
    fun handleCreateAccount(request: CreateAccountRequestPacket) {
        val (ownerId, name, defaultAccount) = request
        request.launch {
            val account = AccountRepository.createAccount(
                ownerId = ownerId,
                name = name,
                defaultAccount = defaultAccount
            )

            request.respond(AccountResponse(account))
        }
    }
}