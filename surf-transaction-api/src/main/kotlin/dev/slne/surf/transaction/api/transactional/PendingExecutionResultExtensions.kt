package dev.slne.surf.transaction.api.transactional

import dev.slne.surf.transaction.api.transaction.Transaction

/**
 * The financial transaction associated with this result, or `null` when none exists.
 *
 * Every outcome except [PendingExecutionResult.ReservationFailed] carries a [Transaction]: it is the
 * committed snapshot for [PendingExecutionResult.Completed] and [PendingExecutionResult.RolledBack],
 * and the pending or original snapshot retained for reconciliation in the remaining outcomes. A
 * [PendingExecutionResult.ReservationFailed] never created a reservation and therefore has no
 * transaction.
 *
 * This is convenient for logging or reconciliation code that only needs the stable transaction
 * identifier and does not care which specific outcome occurred.
 */
val PendingExecutionResult<*>.transactionOrNull: Transaction?
    get() = when (this) {
        is PendingExecutionResult.Completed -> transaction
        is PendingExecutionResult.RolledBack -> transaction
        is PendingExecutionResult.RollbackFailed -> transaction
        is PendingExecutionResult.CommitFailed -> transaction
        is PendingExecutionResult.ExternalFailureRolledBack -> transaction
        is PendingExecutionResult.ExternalFailureRollbackFailed -> transaction
        is PendingExecutionResult.ReservationFailed -> null
    }

/**
 * The value returned by the external operation, or `null` when the operation never produced one.
 *
 * A value is present for [PendingExecutionResult.Completed], [PendingExecutionResult.RolledBack],
 * [PendingExecutionResult.RollbackFailed], and [PendingExecutionResult.CommitFailed]. It is absent
 * when the reservation failed before the operation ran ([PendingExecutionResult.ReservationFailed])
 * or when the operation threw a configured exception instead of returning
 * ([PendingExecutionResult.ExternalFailureRolledBack],
 * [PendingExecutionResult.ExternalFailureRollbackFailed]).
 *
 * Note that a non-null value does not imply success: for [PendingExecutionResult.RolledBack] and
 * [PendingExecutionResult.RollbackFailed] it is the known unsuccessful result that selected rollback.
 */
val <T> PendingExecutionResult<T>.valueOrNull: T?
    get() = when (this) {
        is PendingExecutionResult.Completed -> value
        is PendingExecutionResult.RolledBack -> value
        is PendingExecutionResult.RollbackFailed -> value
        is PendingExecutionResult.CommitFailed -> value
        is PendingExecutionResult.ReservationFailed -> null
        is PendingExecutionResult.ExternalFailureRolledBack -> null
        is PendingExecutionResult.ExternalFailureRollbackFailed -> null
    }

/**
 * `true` when the external operation succeeded and the financial transaction reached committed state.
 *
 * This is the only fully successful outcome. In particular, [PendingExecutionResult.CommitFailed] is
 * not considered completed here: the external side effect took effect, but the commit outcome is
 * indeterminate and needs reconciliation.
 */
val PendingExecutionResult<*>.isCompleted: Boolean
    get() = this is PendingExecutionResult.Completed
