package dev.slne.surf.transaction.microservice.handler.account

import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import dev.slne.surf.transaction.core.common.protocol.account.addMember.AddMemberToAccountRequestPacket
import dev.slne.surf.transaction.core.common.protocol.account.addMember.AddMemberToAccountResponsePacket
import dev.slne.surf.transaction.microservice.db.account.AccountRepository
import kotlinx.coroutines.launch

object AccountAddMemberHandler {

    @RabbitHandler
    fun handleAddMemberToAccount(request: AddMemberToAccountRequestPacket) {
        val (accountId, executor, target) = request
        request.launch {
            val (result, accountOwnerId) = AccountRepository.addMemberToAccount(
                accountId = accountId,
                executor = executor,
                target = target
            )

            request.respond(AddMemberToAccountResponsePacket(result, accountOwnerId))
        }
    }
}