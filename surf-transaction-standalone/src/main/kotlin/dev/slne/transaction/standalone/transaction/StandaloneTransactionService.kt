package dev.slne.transaction.standalone.transaction

import com.google.auto.service.AutoService
import dev.slne.transaction.api.transaction.Transaction
import dev.slne.transaction.api.transaction.TransactionResult
import dev.slne.transaction.api.transaction.TransactionService
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import jakarta.transaction.Transactional
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import java.util.*

@Service
@AutoService(TransactionService::class)
class StandaloneTransactionService(
    private val transactionRepository: TransactionRepository
) : TransactionService {

    override suspend fun persistTransaction(transaction: Transaction): TransactionResult = withContext(Dispatchers.IO) {
        try {
            transactionRepository.save(transaction as TransactionModel)

            TransactionResult.SUCCESS
        } catch (e: Exception) {
            TransactionResult.DATABASE_ERROR
        }
    }

    @Transactional
    override suspend fun rollbackTransaction(transaction: Transaction): Pair<TransactionResult, Transaction?> = withContext(Dispatchers.IO) {
        val oldAmount = transaction.amount
        val newAmount = -oldAmount
        val newTransactionIdResult = runCatching { generateTransactionId() }

        if (newTransactionIdResult.isFailure) {
            return@withContext TransactionResult.TRANSACTION_ID_NOT_GENERATABLE to null
        }

        val oldExtra = transaction.extra
        val newExtra = Object2ObjectOpenHashMap(oldExtra).apply {
            put("rollback", "true")
        }

        val newTransaction = TransactionModel(
            identifier = newTransactionIdResult.getOrThrow(),
            senderUuid = transaction.sender?.uuid,
            receiverUuid = transaction.receiver?.uuid,
            amount = newAmount,
            currency = transaction.currency,
            _extra = newExtra,
        )

        try {
            transactionRepository.save(newTransaction)

            TransactionResult.SUCCESS to newTransaction
        } catch (e: Exception) {
            TransactionResult.DATABASE_ERROR to newTransaction
        }
    }

    @Transactional
    override suspend fun transfer(senderTransaction: Transaction, receiverTransaction: Transaction): TransactionResult = withContext(Dispatchers.IO) {
        TODO("Not yet implemented")
    }

    override suspend fun generateTransactionId(): UUID {
        TODO("Not yet implemented")
    }
}