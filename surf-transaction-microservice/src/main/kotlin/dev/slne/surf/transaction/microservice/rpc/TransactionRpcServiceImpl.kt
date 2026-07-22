package dev.slne.surf.transaction.microservice.rpc

import dev.slne.surf.transaction.core.common.rpc.TransactionRpcService
import dev.slne.surf.transaction.core.common.transaction.TransactionImpl
import dev.slne.surf.transaction.microservice.db.transaction.TransactionRepository
import java.util.UUID

object TransactionRpcServiceImpl : TransactionRpcService {
    override suspend fun beginTransaction(
        transaction: TransactionImpl,
        timeoutMillis: Long
    ) = TransactionRepository.persistPendingTransaction(transaction, timeoutMillis)

    override suspend fun beginTransfer(
        senderTransaction: TransactionImpl,
        receiverTransaction: TransactionImpl,
        timeoutMillis: Long
    ) = TransactionRepository.pendingTransfer(
        senderTransaction,
        receiverTransaction,
        timeoutMillis
    )

    override suspend fun commit(identifier: UUID) = TransactionRepository.commit(identifier)
    override suspend fun rollback(identifier: UUID) = TransactionRepository.rollback(identifier)
    override suspend fun find(identifier: UUID) = TransactionRepository.find(identifier)
}
