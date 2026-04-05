package dev.slne.surf.transaction.microservice.handler.account

import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import dev.slne.surf.transaction.core.common.protocol.account.AccountResponse
import dev.slne.surf.transaction.core.common.protocol.account.findOrCreateDefaultAccountByPlayerUuid.FindOrCreateDefaultAccountByPlayerUuidRequestPacket
import dev.slne.surf.transaction.microservice.db.account.AccountRepository
import kotlinx.coroutines.launch

object FindOrCreateDefaultAccountByPlayerUuidHandler {

    @RabbitHandler
    fun handleFindOrCreateDefaultAccountByPlayerUuid(request: FindOrCreateDefaultAccountByPlayerUuidRequestPacket) {
        request.launch {
            val account = AccountRepository.findOrCreateDefaultAccount(request.playerUuid)
            request.respond(AccountResponse(account))
        }
    }
}