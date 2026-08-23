package dev.slne.surf.transaction.core.common.transactional

import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.PendingTransactionResult
import dev.slne.surf.transaction.api.transaction.Transaction
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.transactional.PendingExecutionDecision
import dev.slne.surf.transaction.api.transactional.PendingExecutionResult
import dev.slne.surf.transaction.api.transactional.PendingRollbackPolicy
import dev.slne.surf.transaction.api.transactional.Transactional
import dev.slne.surf.transaction.core.common.transaction.CoreTransactionService
import java.math.BigDecimal
import java.util.*
import kotlin.time.Duration

/**
 * Shared [TransactionalImpl] delegate.
 */
internal val sharedTransactional = TransactionalImpl()

class TransactionalImpl : Transactional {

    override suspend fun beginDeposit(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): PendingTransactionResult = CoreTransactionService.beginDeposit(
        account,
        initiator,
        amount,
        currency,
        timeout,
        ignoreMinimum,
        *additionalData
    )

    override suspend fun beginWithdrawal(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): PendingTransactionResult = CoreTransactionService.beginWithdrawal(
        account,
        initiator,
        amount,
        currency,
        timeout,
        ignoreMinimum,
        *additionalData
    )

    override suspend fun beginTransfer(
        initiator: UUID,
        sender: Account,
        amount: BigDecimal,
        currency: Currency,
        receiver: Account,
        timeout: Duration,
        ignoreSenderMinimum: Boolean,
        ignoreReceiverMinimum: Boolean,
        additionalSenderData: Set<TransactionData>,
        additionalReceiverData: Set<TransactionData>
    ): PendingTransactionResult = CoreTransactionService.beginTransfer(
        initiator,
        sender,
        amount,
        currency,
        receiver,
        timeout,
        ignoreSenderMinimum,
        ignoreReceiverMinimum,
        additionalSenderData,
        additionalReceiverData
    )

    override suspend fun <T> withPendingDeposit(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration,
        ignoreMinimum: Boolean,
        additionalData: Set<TransactionData>,
        rollbackOn: PendingRollbackPolicy,
        block: suspend (UUID) -> T
    ): PendingExecutionResult<T> {
        val reservation = beginDeposit(
            account,
            initiator,
            amount,
            currency,
            timeout,
            ignoreMinimum,
            *additionalData.toTypedArray()
        )
        return executePendingReservation(
            reservation = reservation,
            rollbackOn = rollbackOn,
            block = block,
            commit = Transaction::commit,
            rollback = Transaction::rollback
        )
    }

    override suspend fun <T> withPendingWithdrawal(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration,
        ignoreMinimum: Boolean,
        additionalData: Set<TransactionData>,
        rollbackOn: PendingRollbackPolicy,
        block: suspend (UUID) -> T
    ): PendingExecutionResult<T> {
        val reservation = beginWithdrawal(
            account,
            initiator,
            amount,
            currency,
            timeout,
            ignoreMinimum,
            *additionalData.toTypedArray()
        )
        return executePendingReservation(
            reservation = reservation,
            rollbackOn = rollbackOn,
            block = block,
            commit = Transaction::commit,
            rollback = Transaction::rollback
        )
    }

    override suspend fun <T> withPendingTransfer(
        initiator: UUID,
        sender: Account,
        amount: BigDecimal,
        currency: Currency,
        receiver: Account,
        timeout: Duration,
        ignoreSenderMinimum: Boolean,
        ignoreReceiverMinimum: Boolean,
        additionalSenderData: Set<TransactionData>,
        additionalReceiverData: Set<TransactionData>,
        rollbackOn: PendingRollbackPolicy,
        block: suspend (UUID) -> T
    ): PendingExecutionResult<T> {
        val reservation = beginTransfer(
            initiator,
            sender,
            amount,
            currency,
            receiver,
            timeout,
            ignoreSenderMinimum,
            ignoreReceiverMinimum,
            additionalSenderData,
            additionalReceiverData
        )
        return executePendingReservation(
            reservation = reservation,
            rollbackOn = rollbackOn,
            block = block,
            commit = Transaction::commit,
            rollback = Transaction::rollback
        )
    }

    override suspend fun <T> withPendingDepositDecision(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration,
        ignoreMinimum: Boolean,
        additionalData: Set<TransactionData>,
        rollbackOn: PendingRollbackPolicy,
        block: suspend (UUID) -> PendingExecutionDecision<T>
    ): PendingExecutionResult<T> {
        val reservation = beginDeposit(
            account,
            initiator,
            amount,
            currency,
            timeout,
            ignoreMinimum,
            *additionalData.toTypedArray()
        )
        return executePendingReservationDecision(
            reservation = reservation,
            rollbackOn = rollbackOn,
            block = block,
            commit = Transaction::commit,
            rollback = Transaction::rollback
        )
    }

    override suspend fun <T> withPendingWithdrawalDecision(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration,
        ignoreMinimum: Boolean,
        additionalData: Set<TransactionData>,
        rollbackOn: PendingRollbackPolicy,
        block: suspend (UUID) -> PendingExecutionDecision<T>
    ): PendingExecutionResult<T> {
        val reservation = beginWithdrawal(
            account,
            initiator,
            amount,
            currency,
            timeout,
            ignoreMinimum,
            *additionalData.toTypedArray()
        )
        return executePendingReservationDecision(
            reservation = reservation,
            rollbackOn = rollbackOn,
            block = block,
            commit = Transaction::commit,
            rollback = Transaction::rollback
        )
    }

    override suspend fun <T> withPendingTransferDecision(
        initiator: UUID,
        sender: Account,
        amount: BigDecimal,
        currency: Currency,
        receiver: Account,
        timeout: Duration,
        ignoreSenderMinimum: Boolean,
        ignoreReceiverMinimum: Boolean,
        additionalSenderData: Set<TransactionData>,
        additionalReceiverData: Set<TransactionData>,
        rollbackOn: PendingRollbackPolicy,
        block: suspend (UUID) -> PendingExecutionDecision<T>
    ): PendingExecutionResult<T> {
        val reservation = beginTransfer(
            initiator,
            sender,
            amount,
            currency,
            receiver,
            timeout,
            ignoreSenderMinimum,
            ignoreReceiverMinimum,
            additionalSenderData,
            additionalReceiverData
        )
        return executePendingReservationDecision(
            reservation = reservation,
            rollbackOn = rollbackOn,
            block = block,
            commit = Transaction::commit,
            rollback = Transaction::rollback
        )
    }

    override suspend fun deposit(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult {
        return CoreTransactionService.deposit(
            account,
            initiator,
            amount,
            currency,
            ignoreMinimum,
            *additionalData
        )
    }

    override suspend fun withdraw(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult {
        return CoreTransactionService.withdraw(
            account,
            initiator,
            amount,
            currency,
            ignoreMinimum,
            *additionalData
        )
    }

    override suspend fun transfer(
        initiator: UUID,
        sender: Account,
        amount: BigDecimal,
        currency: Currency,
        receiver: Account,
        ignoreSenderMinimum: Boolean,
        ignoreReceiverMinimum: Boolean,
        additionalSenderData: Set<TransactionData>,
        additionalReceiverData: Set<TransactionData>
    ): TransactionResult {
        return CoreTransactionService.transfer(
            initiator,
            sender,
            amount,
            currency,
            receiver,
            ignoreSenderMinimum,
            ignoreReceiverMinimum,
            additionalSenderData,
            additionalReceiverData
        )
    }

    override suspend fun balance(
        account: Account,
        currency: Currency
    ): BigDecimal {
        return CoreTransactionService.balance(account, currency)
    }
}
