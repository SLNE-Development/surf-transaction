package dev.slne.surf.transaction.microservice.handler.transaction

import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import dev.slne.surf.transaction.core.common.protocol.transaction.TransactionResultPacket
import dev.slne.surf.transaction.core.common.protocol.transaction.create.CreateTransactionRequestPacket
import dev.slne.surf.transaction.microservice.db.transaction.TransactionRepository
import kotlinx.coroutines.launch

object CreateTransactionHandler {

    @RabbitHandler
    fun handleCreateTransaction(request: CreateTransactionRequestPacket) {
        request.launch {
            val result = TransactionRepository.persistTransaction(request.transaction)
            request.respond(TransactionResultPacket(result))
        }
    }
}