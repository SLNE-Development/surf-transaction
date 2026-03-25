package dev.slne.surf.transaction.core.common.db.transaction

import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.core.common.transaction.TransactionImpl
import java.math.BigDecimal
import java.util.UUID

interface TransactionRepository {

    suspend fun persistTransaction(transaction: TransactionImpl): TransactionResult
    suspend fun transfer(
        senderTransaction: TransactionImpl,
        receiverTransaction: TransactionImpl
    ): TransactionResult

    suspend fun balanceDecimal(accountId: UUID, currency: Currency): BigDecimal

    companion object : TransactionRepository by TransactionRepositoryImpl() {
        fun init() = Unit
    }
}