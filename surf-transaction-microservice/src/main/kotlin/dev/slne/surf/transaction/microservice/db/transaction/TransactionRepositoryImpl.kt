package dev.slne.surf.transaction.microservice.db.transaction

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.*
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.statements.BatchInsertStatement
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.batchInsert
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insertReturning
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.select
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.core.common.transaction.TransactionImpl
import dev.slne.surf.transaction.microservice.db.account.AccountRepository
import dev.slne.surf.transaction.microservice.db.currency.CurrencyRepository
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
        val insertedTransactionRow = TransactionTable.insertReturning {
            it[identifier] = transaction.identifier
            it[initiator] = transaction.initiator
            it[amount] = transaction.amount
            it[currency] = CurrencyRepository.findCurrencyIDByNameQuery(transaction.currency.name)

            transaction.senderAccountId?.let { senderAccount ->
                it[sender] = AccountRepository.findAccountIDByIdQuery(senderAccount)
            }

            transaction.receiverAccountId?.let { receiverAccount ->
                it[receiver] = AccountRepository.findAccountIDByIdQuery(receiverAccount)
            }
        }.single()

        val transactionID = insertedTransactionRow[TransactionTable.id].value
        val receiverID = insertedTransactionRow[TransactionTable.receiver]?.value
        val currencyID = insertedTransactionRow[TransactionTable.currency].value

        TransactionDataTable.batchInsert(transaction.data, shouldReturnGeneratedValues = false) {
            insertTransactionData(transactionID, it)
        }

        if (receiverID != null && !transaction.ignoreMinimumAmount) {
            val balanceAfterTransaction = balanceDecimal0 {
                (TransactionTable.currency eq currencyID) and (TransactionTable.receiver eq receiverID)
            }

            if (balanceAfterTransaction < transaction.currency.minimumAmount) {
                return TransactionResult.ReceiverInsufficientFunds
            }
        }

        return TransactionResult.Success(transaction)
    }

    override suspend fun balanceDecimal(accountId: UUID, currency: Currency): BigDecimal = suspendTransaction {
        balanceDecimal0 {
            (TransactionTable.currency eqSubQuery CurrencyRepository.findCurrencyIDByNameQuery(currency.name)) and
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