package dev.slne.surf.transaction.microservice.handler.transaction

import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import dev.slne.surf.transaction.core.common.protocol.transaction.TransactionResultPacket
import dev.slne.surf.transaction.core.common.protocol.transaction.transfer.TransferTransactionCreateRequestPacket
import dev.slne.surf.transaction.microservice.db.transaction.TransactionRepository
import kotlinx.coroutines.launch

object TransferTransactionCreateHandler {

    @RabbitHandler
    fun handleTransferTransactionCreate(request: TransferTransactionCreateRequestPacket) {
        val (senderTransaction, receiverTransaction) = request
        request.launch {
            val result = TransactionRepository.transfer(senderTransaction, receiverTransaction)
            request.respond(TransactionResultPacket(result))
        }
    }
}