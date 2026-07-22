package dev.slne.surf.transaction.core.common.rpc

import dev.slne.surf.rabbitmq.api.rpc.RpcService
import dev.slne.surf.transaction.api.transaction.PendingTransactionResult
import dev.slne.surf.transaction.api.transaction.Transaction
import dev.slne.surf.transaction.api.transaction.TransactionCommitResult
import dev.slne.surf.transaction.api.transaction.TransactionRollbackResult
import dev.slne.surf.transaction.core.common.transaction.TransactionImpl
import java.util.*

/** Internal RPC contract for persistent pending-transaction lifecycle operations. */
@RpcService
interface TransactionRpcService {
    suspend fun beginTransaction(
        transaction: TransactionImpl,
        timeoutMillis: Long
    ): PendingTransactionResult

    suspend fun beginTransfer(
        senderTransaction: TransactionImpl,
        receiverTransaction: TransactionImpl,
        timeoutMillis: Long
    ): PendingTransactionResult

    suspend fun commit(identifier: UUID): TransactionCommitResult
    suspend fun rollback(identifier: UUID): TransactionRollbackResult
    suspend fun find(identifier: UUID): Transaction?
}
