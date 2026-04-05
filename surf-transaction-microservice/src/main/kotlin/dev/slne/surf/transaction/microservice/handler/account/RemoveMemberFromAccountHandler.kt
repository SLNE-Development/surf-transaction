package dev.slne.surf.transaction.microservice.handler.account

import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import dev.slne.surf.transaction.core.common.protocol.account.removeMember.RemoveMemberFromAccountRequestPacket
import dev.slne.surf.transaction.core.common.protocol.account.removeMember.RemoveMemberFromAccountResponsePacket
import dev.slne.surf.transaction.microservice.db.account.AccountRepository
import kotlinx.coroutines.launch

object RemoveMemberFromAccountHandler {

    @RabbitHandler
    fun handleRemoveMemberFromAccount(request: RemoveMemberFromAccountRequestPacket) {
        val (accountId, executor, target) = request
        request.launch {
            val (result, accountOwnerUuid) = AccountRepository.removeMemberFromAccount(
                accountId = accountId,
                executor = executor,
                target = target
            )

            request.respond(RemoveMemberFromAccountResponsePacket(result, accountOwnerUuid))
        }
    }
}