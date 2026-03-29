package dev.slne.surf.transaction.microservice.db.transaction

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.*
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.statements.BatchInsertStatement
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.batchInsert
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insertReturning
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.select
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.surfapi.core.api.util.SerializableError
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.core.common.transaction.TransactionImpl
import dev.slne.surf.transaction.microservice.db.account.AccountRepository
import dev.slne.surf.transaction.microservice.db.currency.CurrencyRepository
import dev.slne.surf.transaction.microservice.db.currency.CurrencyTable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import java.math.BigDecimal
import java.util.*

class TransactionRepositoryImpl : TransactionRepository {
    override suspend fun persistTransaction(transaction: TransactionImpl) = suspendTransaction {
        persistTransaction0(transaction).also { result ->
            if (!result.success) {
                rollback()
            }
        }
    }

    suspend fun persistTransaction0(transaction: TransactionImpl): TransactionResult {
        val currencyRow = CurrencyTable
            .select(CurrencyTable.id, CurrencyTable.minimumAmount)
            .where { CurrencyTable.name eq transaction.currencyName }
            .limit(1)
            .singleOrNull()
            ?: return TransactionResult.DatabaseError(
                SerializableError(
                    "CURRENCY_NOT_FOUND",
                    "Currency not found: ${transaction.currencyName}"
                )
            )

        val currencyId = currencyRow[CurrencyTable.id]
        val minimumAmount = currencyRow[CurrencyTable.minimumAmount]

        val senderId = transaction.senderAccountId?.let { senderAccountId ->
            AccountRepository.findAccountIDByIdQuery(senderAccountId)
        }

        val receiverId = transaction.receiverAccountId?.let { receiverAccountId ->
            AccountRepository.findAccountIDByIdQuery(receiverAccountId)
        }

        val insertedTransactionRow = TransactionTable.insertReturning {
            it[identifier] = transaction.identifier
            it[initiator] = transaction.initiator
            it[amount] = transaction.amount
            it[currency] = CurrencyRepository.findCurrencyIDByNameQuery(transaction.currencyName)
            senderId?.let { sender ->
                it[TransactionTable.sender] = sender
            }

            receiverId?.let { receiver ->
                it[TransactionTable.receiver] = receiver
            }
        }.single()

        val transactionId = insertedTransactionRow[TransactionTable.id].value
        val insertedReceiverId = insertedTransactionRow[TransactionTable.receiver]?.value

        if (insertedReceiverId != null && !transaction.ignoreMinimumAmount) {
            val balanceAfterTransaction = balanceDecimal0 {
                (TransactionTable.currency eq currencyId.value) and
                        (TransactionTable.receiver eq insertedReceiverId)
            }

            if (balanceAfterTransaction < minimumAmount) {
                return TransactionResult.ReceiverInsufficientFunds
            }
        }

        TransactionDataTable.batchInsert(transaction.data, shouldReturnGeneratedValues = false) {
            insertTransactionData(transactionId, it)
        }

        return TransactionResult.Success(transaction)
    }

    override suspend fun balanceDecimal(accountId: UUID, currencyName: String): BigDecimal = suspendTransaction {
        balanceDecimal0 {
            (TransactionTable.currency eqSubQuery CurrencyRepository.findCurrencyIDByNameQuery(currencyName)) and
                    (TransactionTable.receiver eqSubQuery AccountRepository.findAccountIDByIdQuery(accountId))
        }
    }

    suspend fun balanceDecimal0(where: () -> Op<Boolean>): BigDecimal = TransactionTable
        .select(TransactionTable.amount.sum())
        .where(where)
        .map { it[TransactionTable.amount.sum()] }
        .singleOrNull() ?: BigDecimal.ZERO

    override suspend fun transfer(
        senderTransaction: TransactionImpl,
        receiverTransaction: TransactionImpl
    ): TransactionResult = suspendTransaction {
        val senderResult = persistTransaction0(senderTransaction)
        if (!senderResult.success) {
            rollback()
            return@suspendTransaction if (senderResult == TransactionResult.ReceiverInsufficientFunds) TransactionResult.SenderInsufficientFunds else senderResult
        }

        val receiverResult = persistTransaction0(receiverTransaction)
        if (!receiverResult.success) {
            rollback()
            return@suspendTransaction receiverResult
        }

        return@suspendTransaction TransactionResult.TransferSuccess(
            senderTransaction,
            receiverTransaction
        )
    }

    private fun BatchInsertStatement.insertTransactionData(
        transactionID: ULong,
        data: TransactionData
    ) {
        this[TransactionDataTable.transaction] = transactionID
        this[TransactionDataTable.key] = data.key
        this[TransactionDataTable.value] = data.value
    }
}