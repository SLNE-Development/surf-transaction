package dev.slne.surf.transaction.microservice.db.transaction

import dev.slne.surf.api.core.util.SerializableError
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.transaction.api.transaction.*
import dev.slne.surf.transaction.core.common.transaction.TransactionImpl
import dev.slne.surf.transaction.microservice.db.transaction.access.TransactionLifecycle
import dev.slne.surf.transaction.microservice.db.transaction.access.TransactionReader
import dev.slne.surf.transaction.microservice.db.transaction.access.TransactionWriteResult
import dev.slne.surf.transaction.microservice.db.transaction.access.TransactionWriter
import dev.slne.surf.transaction.microservice.db.transaction.support.TransactionErrors
import dev.slne.surf.transaction.microservice.db.transaction.support.TransactionRules
import java.math.BigDecimal
import java.util.*
import kotlin.coroutines.cancellation.CancellationException

/**
 * Orchestrates the persistence components and maps their outcomes to the API result types.
 *
 * Error contract: committed operations report unexpected failures as
 * [TransactionResult.DatabaseError], while pending operations and lifecycle transitions let them
 * propagate to the RPC layer.
 */
internal class TransactionRepositoryImpl : TransactionRepository {
    private val reader = TransactionReader()
    private val writer = TransactionWriter(reader)
    private val lifecycle = TransactionLifecycle(reader)

    override suspend fun persistTransaction(
        transaction: TransactionImpl
    ): TransactionResult = reportUnexpectedAs(TransactionResult::DatabaseError) {
        writer.writeCommitted(listOf(transaction)).asTransactionResult(
            onInsufficientFunds = TransactionResult.ReceiverInsufficientFunds,
            onWritten = { TransactionResult.Success(it.single()) }
        )
    }

    override suspend fun persistPendingTransaction(
        transaction: TransactionImpl,
        timeoutMillis: Long
    ): PendingTransactionResult {
        if (timeoutMillis <= 0) return PendingTransactionResult.InvalidTimeout

        return writer.writePending(listOf(transaction), timeoutMillis).asPendingResult(
            onInsufficientFunds = PendingTransactionResult.ReceiverInsufficientFunds,
            onWritten = { PendingTransactionResult.Created(it.single()) }
        )
    }

    override suspend fun transfer(
        senderTransaction: TransactionImpl,
        receiverTransaction: TransactionImpl
    ): TransactionResult {
        if (!TransactionRules.isMirroredTransferPair(senderTransaction, receiverTransaction)) {
            return TransactionResult.DatabaseError(TransactionErrors.invalidTransferPair())
        }

        return reportUnexpectedAs(TransactionResult::DatabaseError) {
            writer.writeCommitted(listOf(senderTransaction, receiverTransaction))
                .asTransactionResult(
                    onInsufficientFunds = TransactionResult.SenderInsufficientFunds,
                    onWritten = { (sender, receiver) ->
                        TransactionResult.TransferSuccess(sender, receiver)
                    }
                )
        }
    }

    override suspend fun pendingTransfer(
        senderTransaction: TransactionImpl,
        receiverTransaction: TransactionImpl,
        timeoutMillis: Long
    ): PendingTransactionResult {
        if (timeoutMillis <= 0) return PendingTransactionResult.InvalidTimeout
        if (!TransactionRules.isMirroredTransferPair(senderTransaction, receiverTransaction)) {
            return PendingTransactionResult.InvalidRequest(TransactionErrors.invalidTransferPair())
        }

        return writer.writePending(listOf(senderTransaction, receiverTransaction), timeoutMillis)
            .asPendingResult(
                onInsufficientFunds = PendingTransactionResult.SenderInsufficientFunds,
                onWritten = { (sender, receiver) ->
                    PendingTransactionResult.TransferCreated(sender, receiver)
                }
            )
    }

    override suspend fun commit(identifier: UUID): TransactionCommitResult {
        val transition = lifecycle.transition(identifier, TransactionState.COMMITTED)
            ?: return TransactionCommitResult.NotFound

        return if (transition.applied) {
            TransactionCommitResult.Committed(transition.snapshot)
        } else {
            TransactionRules.commitOutcome(transition.snapshot)
        }
    }

    override suspend fun rollback(identifier: UUID): TransactionRollbackResult {
        val transition = lifecycle.transition(identifier, TransactionState.ROLLED_BACK)
            ?: return TransactionRollbackResult.NotFound

        return if (transition.applied) {
            TransactionRollbackResult.RolledBack(transition.snapshot)
        } else {
            TransactionRules.rollbackOutcome(transition.snapshot)
        }
    }

    override suspend fun find(
        identifier: UUID
    ): Transaction? = suspendTransaction(readOnly = true) {
        reader.find(identifier)?.currentSnapshot()
    }

    override suspend fun expireTransactions(): Int = reportUnexpectedAs(onError = { 0 }) {
        lifecycle.expireAllDue()
    }

    override suspend fun balanceDecimal(
        accountId: UUID,
        currencyName: String
    ): BigDecimal = suspendTransaction(readOnly = true) {
        reader.availableBalance(accountId, currencyName)
    }

    private suspend fun <T> reportUnexpectedAs(
        onError: (SerializableError) -> T,
        block: suspend () -> T
    ): T = try {
        block()
    } catch (cause: CancellationException) {
        throw cause
    } catch (cause: Exception) {
        onError(TransactionErrors.unexpected(cause))
    }
}

private inline fun TransactionWriteResult.asTransactionResult(
    onInsufficientFunds: TransactionResult,
    onWritten: (List<TransactionImpl>) -> TransactionResult
): TransactionResult = when (this) {
    is TransactionWriteResult.Written -> onWritten(transactions)
    TransactionWriteResult.InsufficientFunds -> onInsufficientFunds
    TransactionWriteResult.UnknownAccountOrCurrency ->
        TransactionResult.DatabaseError(TransactionErrors.unknownAccountOrCurrency())

    is TransactionWriteResult.IdentifierConflict ->
        TransactionResult.DatabaseError(TransactionErrors.identifierConflict(identifier))

    is TransactionWriteResult.ConstraintViolation ->
        TransactionResult.DatabaseError(TransactionErrors.constraintViolation(violation))
}

private inline fun TransactionWriteResult.asPendingResult(
    onInsufficientFunds: PendingTransactionResult,
    onWritten: (List<TransactionImpl>) -> PendingTransactionResult
): PendingTransactionResult = when (this) {
    is TransactionWriteResult.Written -> onWritten(transactions)
    TransactionWriteResult.InsufficientFunds -> onInsufficientFunds
    TransactionWriteResult.UnknownAccountOrCurrency -> PendingTransactionResult.InvalidRequest(
        TransactionErrors.unknownAccountOrCurrency()
    )

    is TransactionWriteResult.IdentifierConflict -> PendingTransactionResult.InvalidRequest(
        TransactionErrors.identifierConflict(identifier)
    )

    is TransactionWriteResult.ConstraintViolation -> PendingTransactionResult.InvalidRequest(
        TransactionErrors.constraintViolation(violation)
    )
}
