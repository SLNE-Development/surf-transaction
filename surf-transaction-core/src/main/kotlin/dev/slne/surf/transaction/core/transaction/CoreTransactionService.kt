package dev.slne.surf.transaction.core.transaction

import com.google.auto.service.AutoService
import dev.slne.surf.transaction.api.transaction.Transaction
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.TransactionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.kyori.adventure.util.Services.Fallback
import java.util.*

@AutoService(TransactionService::class)
class CoreTransactionService : TransactionService, Fallback {

    override suspend fun persistTransaction(transaction: Transaction): TransactionResult =
        withContext(Dispatchers.IO) {
            TODO("Send packet")
        }

    override suspend fun rollbackTransaction(transaction: Transaction): Pair<TransactionResult, Transaction> =
        withContext(Dispatchers.IO) {
            TODO("Send packet")
        }

    override suspend fun transfer(
        senderTransaction: Transaction,
        receiverTransaction: Transaction
    ): TransactionResult = withContext(Dispatchers.IO) {
        TODO("Send packet")
    }

    override suspend fun generateTransactionId(): UUID {
        TODO("Send packet")
    }
}