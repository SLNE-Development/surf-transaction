package dev.slne.surf.transaction.core.common.db.transaction

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.and
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.statements.BatchInsertStatement
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.statements.InsertStatement
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.sum
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.batchInsert
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.select
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.core.common.db.account.AccountRepository
import dev.slne.surf.transaction.core.common.db.currency.CurrencyRepository
import dev.slne.surf.transaction.core.common.db.currency.CurrencyTable
import dev.slne.surf.transaction.core.common.transaction.TransactionImpl
import kotlinx.coroutines.flow.map
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
        val senderID =
            transaction.senderAccountId?.let { AccountRepository.Companion.findAccountIDByAccountId(it) }
        val receiverID =
            transaction.receiverAccountId?.let { AccountRepository.Companion.findAccountIDByAccountId(it) }
        val currencyID = CurrencyRepository.Companion.findCurrencyIDByName(transaction.currencyName)
            ?: error("Currency not found")

        val createdTransactionID = TransactionTable.insertAndGetId {
            insertTransaction(transaction, senderID, receiverID, currencyID, it)
        }.value

        TransactionDataTable.batchInsert(transaction.data, shouldReturnGeneratedValues = false) {
            insertTransactionData(createdTransactionID, it)
        }

        if (receiverID != null && !transaction.ignoreMinimumAmount) {
            val balanceAfterTransaction = balanceDecimal0(receiverID, currencyID)
            if (balanceAfterTransaction < transaction.currency.minimumAmount) {
                return TransactionResult.ReceiverInsufficientFunds
            }
        }

        return TransactionResult.Success(transaction)
    }

    override suspend fun balanceDecimal(accountId: UUID, currency: Currency): BigDecimal =
        suspendTransaction {
            val accountID =
                AccountRepository.Companion.findAccountIDByAccountId(accountId) ?: error("Account not found")
            val currencyID = CurrencyRepository.Companion.findCurrencyIDByName(currency.name)
                ?: error("Currency not found")
            balanceDecimal0(accountID, currencyID)
        }


    suspend fun balanceDecimal0(
        accountID: ULong,
        currencyID: ULong
    ): BigDecimal {
        return TransactionTable
            .innerJoin(CurrencyTable)
            .select(TransactionTable.amount.sum())
            .where {
                (CurrencyTable.id eq currencyID) and
                        (TransactionTable.receiver eq accountID)
            }
            .map { it[TransactionTable.amount.sum()] }
            .singleOrNull() ?: BigDecimal.ZERO
    }

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

    private fun TransactionTable.insertTransaction(
        transaction: TransactionImpl,
        senderID: ULong?,
        receiverID: ULong?,
        currencyID: ULong,
        smt: InsertStatement<*>
    ) {
        smt[identifier] = transaction.identifier
        smt[initiator] = transaction.initiator
        smt[sender] = senderID
        smt[receiver] = receiverID
        smt[amount] = transaction.amount
        smt[currency] = currencyID
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