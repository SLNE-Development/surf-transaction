package dev.slne.surf.transaction.api.transaction

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import java.util.UUID

@InternalTransactionApi
interface TransactionService {

    suspend fun commit(identifier: UUID): TransactionCommitResult

    suspend fun rollback(identifier: UUID): TransactionRollbackResult

    suspend fun find(identifier: UUID): Transaction?

    companion object : TransactionService by instance {
        val INSTANCE get() = instance
    }
}

private val instance = requiredService<TransactionService>()
