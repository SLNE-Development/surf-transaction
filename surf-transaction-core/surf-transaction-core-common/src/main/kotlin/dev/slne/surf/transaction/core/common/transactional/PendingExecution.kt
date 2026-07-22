package dev.slne.surf.transaction.core.common.transactional

import dev.slne.surf.transaction.api.transaction.PendingTransactionResult
import dev.slne.surf.transaction.api.transaction.Transaction
import dev.slne.surf.transaction.api.transaction.TransactionCommitResult
import dev.slne.surf.transaction.api.transaction.TransactionRollbackResult
import dev.slne.surf.transaction.api.transactional.PendingExecutionDecision
import dev.slne.surf.transaction.api.transactional.PendingExecutionResult
import dev.slne.surf.transaction.api.transactional.PendingLifecycleFailure
import dev.slne.surf.transaction.api.transactional.PendingRollbackPolicy
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

internal suspend fun <T> executePendingReservation(
    reservation: PendingTransactionResult,
    rollbackOn: PendingRollbackPolicy,
    block: suspend (UUID) -> T,
    commit: suspend (Transaction) -> TransactionCommitResult,
    rollback: suspend (Transaction) -> TransactionRollbackResult
): PendingExecutionResult<T> = executePendingReservationDecision(
    reservation = reservation,
    rollbackOn = rollbackOn,
    block = { identifier ->
        PendingExecutionDecision.Commit(block(identifier))
    },
    commit = commit,
    rollback = rollback
)

internal suspend fun <T> executePendingReservationDecision(
    reservation: PendingTransactionResult,
    rollbackOn: PendingRollbackPolicy,
    block: suspend (UUID) -> PendingExecutionDecision<T>,
    commit: suspend (Transaction) -> TransactionCommitResult,
    rollback: suspend (Transaction) -> TransactionRollbackResult
): PendingExecutionResult<T> {
    val pending = when (reservation) {
        is PendingTransactionResult.Created -> reservation.transaction
        is PendingTransactionResult.TransferCreated -> reservation.senderTransaction
        else -> return PendingExecutionResult.ReservationFailed(reservation)
    }

    val decision = try {
        block(pending.identifier)
    } catch (cause: Throwable) {
        cause.rethrowIfCancellationOrFatal()
        if (!rollbackOn.shouldRollback(cause)) {
            throw cause
        }

        val rollbackResult = try {
            rollback(pending)
        } catch (rollbackCause: Throwable) {
            rollbackCause.rethrowIfCancellationOrFatal()
            return PendingExecutionResult.ExternalFailureRollbackFailed(
                cause = cause,
                transaction = pending,
                failure = PendingLifecycleFailure.Thrown(rollbackCause)
            )
        }

        return when (rollbackResult) {
            is TransactionRollbackResult.RolledBack ->
                PendingExecutionResult.ExternalFailureRolledBack(
                    cause,
                    rollbackResult.transaction
                )

            is TransactionRollbackResult.AlreadyRolledBack ->
                PendingExecutionResult.ExternalFailureRolledBack(
                    cause,
                    rollbackResult.transaction
                )

            else -> PendingExecutionResult.ExternalFailureRollbackFailed(
                cause = cause,
                transaction = pending,
                failure = PendingLifecycleFailure.TypedResult(rollbackResult)
            )
        }
    }

    return when (decision) {
        is PendingExecutionDecision.Commit -> commit(
            pending = pending,
            value = decision.value,
            commit = commit
        )

        is PendingExecutionDecision.Rollback -> rollback(
            pending = pending,
            value = decision.value,
            rollback = rollback
        )
    }
}

private suspend fun <T> commit(
    pending: Transaction,
    value: T,
    commit: suspend (Transaction) -> TransactionCommitResult
): PendingExecutionResult<T> {
    val commitResult = try {
        commit(pending)
    } catch (cause: Throwable) {
        cause.rethrowIfCancellationOrFatal()
        return PendingExecutionResult.CommitFailed(
            value,
            pending,
            PendingLifecycleFailure.Thrown(cause)
        )
    }

    return when (commitResult) {
        is TransactionCommitResult.Committed ->
            PendingExecutionResult.Completed(value, commitResult.transaction)

        is TransactionCommitResult.AlreadyCommitted ->
            PendingExecutionResult.Completed(value, commitResult.transaction)

        else -> PendingExecutionResult.CommitFailed(
            value,
            pending,
            PendingLifecycleFailure.TypedResult(commitResult)
        )
    }
}

private suspend fun <T> rollback(
    pending: Transaction,
    value: T,
    rollback: suspend (Transaction) -> TransactionRollbackResult
): PendingExecutionResult<T> {
    val rollbackResult = try {
        rollback(pending)
    } catch (cause: Throwable) {
        cause.rethrowIfCancellationOrFatal()
        return PendingExecutionResult.RollbackFailed(
            value,
            pending,
            PendingLifecycleFailure.Thrown(cause)
        )
    }

    return when (rollbackResult) {
        is TransactionRollbackResult.RolledBack ->
            PendingExecutionResult.RolledBack(value, rollbackResult.transaction)

        is TransactionRollbackResult.AlreadyRolledBack ->
            PendingExecutionResult.RolledBack(value, rollbackResult.transaction)

        else -> PendingExecutionResult.RollbackFailed(
            value,
            pending,
            PendingLifecycleFailure.TypedResult(rollbackResult)
        )
    }
}

private fun Throwable.rethrowIfCancellationOrFatal() {
    if (this is CancellationException || isFatalJvmError()) {
        throw this
    }
}

@Suppress("DEPRECATION", "removal") // ThreadDeath is deprecated
private fun Throwable.isFatalJvmError(): Boolean =
    this is VirtualMachineError || this is LinkageError || this is ThreadDeath
