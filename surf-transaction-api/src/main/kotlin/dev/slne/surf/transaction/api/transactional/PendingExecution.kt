package dev.slne.surf.transaction.api.transactional

import dev.slne.surf.transaction.api.transaction.PendingTransactionResult
import dev.slne.surf.transaction.api.transaction.Transaction
import dev.slne.surf.transaction.api.transaction.TransactionCommitResult
import dev.slne.surf.transaction.api.transaction.TransactionRollbackResult
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet

/**
 * Decides whether a known external-operation failure proves that a pending financial transaction
 * should be rolled back.
 *
 * Lambdas are accepted through Kotlin SAM conversion. The default is [PendingRollbackPolicy.Never],
 * because an unknown exception or lost response does not prove that the external operation failed.
 * Coroutine cancellation and fatal JVM errors are always rethrown without consulting this policy.
 */
fun interface PendingRollbackPolicy {
    /** Returns `true` only when [cause] makes rollback safe. */
    fun shouldRollback(cause: Throwable): Boolean

    /**
     * Creates a policy that rolls back when either this policy or [other] permits the rollback.
     *
     * The returned policy evaluates this policy first. If it returns `true`, [other] is not
     * evaluated.
     *
     * @param other the additional rollback policy
     * @return a policy combining both policies using logical OR
     */
    infix fun or(other: PendingRollbackPolicy): PendingRollbackPolicy =
        PendingRollbackPolicy { cause ->
            shouldRollback(cause) || other.shouldRollback(cause)
        }

    /**
     * Creates a policy that rolls back only when both this policy and [other] permit the rollback.
     *
     * The returned policy evaluates this policy first. If it returns `false`, [other] is not
     * evaluated.
     *
     * @param other the additional rollback policy
     * @return a policy combining both policies using logical AND
     */
    infix fun and(other: PendingRollbackPolicy): PendingRollbackPolicy =
        PendingRollbackPolicy { cause ->
            shouldRollback(cause) && other.shouldRollback(cause)
        }

    /**
     * Creates a policy that returns the inverse result of this policy.
     *
     * A cause accepted by this policy is rejected by the returned policy, and a cause rejected by
     * this policy is accepted.
     *
     * Be careful when using this method with broad policies. Accepting every failure not matched by
     * another policy may include ambiguous failures for which rollback is not known to be safe.
     *
     * @return a policy that negates this policy
     */
    fun negate(): PendingRollbackPolicy = PendingRollbackPolicy { cause -> !shouldRollback(cause) }

    /**
     * Creates a policy that applies this policy only to the deepest reachable cause.
     *
     * Cause-chain traversal stops when no further cause exists or when a cycle is detected. If a
     * cycle exists, the last distinct throwable reached before revisiting an existing throwable is
     * treated as the root cause.
     *
     * Wrapper exceptions are ignored entirely. Use [includeCauses] when both wrappers and nested
     * causes should be considered.
     *
     * @return a policy that evaluates only the deepest reachable cause
     */
    fun onRootCause(): PendingRollbackPolicy = PendingRollbackPolicy { throwable ->
        val visited = ReferenceOpenHashSet<Throwable>(4)
        var current = throwable

        visited.add(current)

        while (true) {
            val next = current.cause ?: break

            if (!visited.add(next)) {
                break
            }

            current = next
        }

        shouldRollback(current)
    }

    /**
     * Creates a policy that applies this policy to the supplied throwable and its complete cause
     * chain.
     *
     * The returned policy permits rollback when this policy accepts the supplied throwable itself
     * or any throwable reachable through [Throwable.cause].
     *
     * Cause-chain traversal stops when no further cause exists or when a cycle is detected.
     *
     * This is useful when a known rollback-safe failure may be wrapped in one or more completion,
     * transport, or infrastructure exceptions.
     *
     * Inspecting every cause can be too broad when arbitrary exceptions may appear in the chain.
     * Use [unwrap] instead when only specific, known wrapper types should be traversed.
     *
     * @return a policy that evaluates this policy against the complete cause chain
     */
    fun includeCauses(): PendingRollbackPolicy = includeCauses(Int.MAX_VALUE)

    /**
     * Creates a policy that applies this policy to the supplied throwable and its cause chain up to
     * [maxDepth].
     *
     * A depth of `0` checks only the supplied throwable. A depth of `1` additionally checks its
     * direct cause. Cause-chain traversal stops earlier when no further cause exists or when a cycle
     * is detected.
     *
     * @param maxDepth the maximum number of cause links to follow
     * @return a policy that evaluates this policy against the bounded cause chain
     * @throws IllegalArgumentException if [maxDepth] is negative
     */
    fun includeCauses(maxDepth: Int): PendingRollbackPolicy {
        require(maxDepth >= 0) { "maxDepth must not be negative" }

        return PendingRollbackPolicy { throwable ->
            val visited = ReferenceOpenHashSet<Throwable>(minOf(maxDepth, 5) + 1)
            var current: Throwable? = throwable
            var depth = 0

            while (
                current != null &&
                depth <= maxDepth &&
                visited.add(current)
            ) {
                if (shouldRollback(current)) {
                    return@PendingRollbackPolicy true
                }

                current = current.cause
                depth++
            }

            false
        }
    }

    /**
     * Creates a policy that checks the supplied throwable and its direct cause.
     *
     * This is equivalent to calling `includeCauses(maxDepth = 1)`.
     *
     * It is useful when a failure may be wrapped exactly once, but recursively inspecting arbitrary
     * nested causes would be too broad.
     *
     * @return a policy that evaluates this policy against the throwable and its direct cause
     */
    fun includeDirectCause(): PendingRollbackPolicy = includeCauses(maxDepth = 1)

    /**
     * Creates a policy that removes known wrapper exceptions before evaluating this policy.
     *
     * Starting with the supplied throwable, the returned policy follows [Throwable.cause] while
     * [isWrapper] accepts the current throwable. The first throwable not accepted by [isWrapper] is
     * evaluated by this policy.
     *
     * Unwrapping stops when the current throwable has no cause or when a cause-chain cycle is
     * detected. A wrapper without a cause is evaluated directly.
     *
     * This is generally safer than [includeCauses] when only specific wrapper types, such as
     * completion or execution exceptions, should be ignored.
     *
     * @param isWrapper determines which throwable types should be unwrapped
     * @return a policy that evaluates this policy after removing accepted wrappers
     */
    fun unwrap(
        isWrapper: (Throwable) -> Boolean
    ): PendingRollbackPolicy = PendingRollbackPolicy { throwable ->
        val visited = ReferenceOpenHashSet<Throwable>(4)
        var current = throwable

        while (visited.add(current) && isWrapper(current)) {
            val next = current.cause ?: break

            if (next in visited) {
                break
            }

            current = next
        }

        shouldRollback(current)
    }

    companion object {
        /** A conservative policy that never rolls back automatically. */
        val Never = PendingRollbackPolicy { false }

        /**
         * An aggressive policy that rolls back for every failure.
         *
         * Warning: Use this policy only when the external operation is guaranteed to be atomic or
         * idempotent, or when every reported failure guarantees that no external side effect
         * occurred. Exceptions such as timeouts and lost responses may happen after the external
         * operation has already completed successfully. Rolling back in such cases may cause
         * duplicated funds or otherwise inconsistent state. Coroutine cancellation and fatal JVM
         * errors are always rethrown before this policy is evaluated.
         */
        val Always = PendingRollbackPolicy { true }

        /**
         * Creates a policy that rolls back for exceptions assignable to [T].
         */
        inline fun <reified T : Throwable> on(): PendingRollbackPolicy =
            PendingRollbackPolicy { it is T }

        /**
         * Creates a policy that rolls back only when the failure has exactly the type [T].
         *
         * Unlike [on], subclasses of [T] are not accepted. Only the supplied throwable itself is
         * checked.
         *
         * Exact type checks should be used only when subclasses do not inherit the same rollback
         * safety guarantee.
         *
         * @param T the exact throwable type that proves rollback is safe
         * @return a policy accepting only failures whose runtime class is exactly [T]
         */
        inline fun <reified T : Throwable> exactly(): PendingRollbackPolicy =
            PendingRollbackPolicy { cause ->
                cause.javaClass == T::class.java
            }

        /**
         * Creates a policy that rolls back for every failure except those assignable to [T].
         *
         * Subclasses of [T] are excluded as well.
         *
         * This policy is inherently broad and should only be used when every non-[T] failure is
         * known to make rollback safe. Unknown failures, timeouts, and cancellations may otherwise
         * be accepted accidentally.
         *
         * @param T the throwable type for which rollback must not occur
         * @return a policy accepting every failure not assignable to [T]
         */
        inline fun <reified T : Throwable> except(): PendingRollbackPolicy =
            PendingRollbackPolicy { it !is T }

        /**
         * Creates a policy that rolls back for failures assignable to any of the supplied [types].
         *
         * Subclasses and implementations of the supplied classes are accepted. Only the supplied
         * throwable itself is checked.
         *
         * @param types the throwable types that prove rollback is safe
         * @return a policy accepting failures matching at least one supplied type
         */
        fun onAny(vararg types: Class<out Throwable>): PendingRollbackPolicy =
            PendingRollbackPolicy { cause ->
                types.any { it.isInstance(cause) }
            }

        /**
         * Creates a policy that permits rollback when at least one supplied policy permits it.
         *
         * Policies are evaluated in iteration order. Evaluation stops after the first policy that
         * returns `true`.
         *
         * An empty collection produces a policy equivalent to [Never].
         *
         * @param policies the policies to combine
         * @return a policy combining all supplied policies using logical OR
         */
        fun anyOf(
            policies: Iterable<PendingRollbackPolicy>
        ): PendingRollbackPolicy =
            PendingRollbackPolicy { cause ->
                policies.any { policy ->
                    policy.shouldRollback(cause)
                }
            }

        /**
         * Creates a policy that permits rollback when at least one supplied policy permits it.
         *
         * Policies are evaluated in argument order. Evaluation stops after the first policy that
         * returns `true`.
         *
         * Supplying no policies produces a policy equivalent to [Never].
         *
         * @param policies the policies to combine
         * @return a policy combining all supplied policies using logical OR
         */
        fun anyOf(
            vararg policies: PendingRollbackPolicy
        ): PendingRollbackPolicy = anyOf(policies.asIterable())

        /**
         * Creates a policy that permits rollback only when every supplied policy permits it.
         *
         * Policies are evaluated in iteration order. Evaluation stops after the first policy that
         * returns `false`.
         *
         * An empty collection produces a policy equivalent to [Always], following the identity
         * value of logical AND.
         *
         * @param policies the policies to combine
         * @return a policy combining all supplied policies using logical AND
         */
        fun allOf(
            policies: Iterable<PendingRollbackPolicy>
        ): PendingRollbackPolicy =
            PendingRollbackPolicy { cause ->
                policies.all { policy ->
                    policy.shouldRollback(cause)
                }
            }

        /**
         * Creates a policy that permits rollback only when every supplied policy permits it.
         *
         * Policies are evaluated in argument order. Evaluation stops after the first policy that
         * returns `false`.
         *
         * Supplying no policies produces a policy equivalent to [Always], following the identity
         * value of logical AND.
         *
         * @param policies the policies to combine
         * @return a policy combining all supplied policies using logical AND
         */
        fun allOf(
            vararg policies: PendingRollbackPolicy
        ): PendingRollbackPolicy = allOf(policies.asIterable())

        /**
         * Creates a rollback policy from an arbitrary [predicate].
         *
         * The predicate must return `true` only for failures that prove the external operation did
         * not complete successfully or produce relevant side effects.
         *
         * @param predicate the predicate used to evaluate reported failures
         * @return a policy backed by [predicate]
         */
        fun matching(
            predicate: (Throwable) -> Boolean
        ): PendingRollbackPolicy = PendingRollbackPolicy(predicate)
    }
}

/**
 * Selects the financial lifecycle action after an external operation returns normally.
 *
 * Use this with the `withPending*Decision` methods when an external API reports its outcome as a
 * value instead of throwing an exception. For example, a method returning `false` can map that
 * value to [Rollback], while `true` maps to [Commit]. The convenience method performs the selected
 * lifecycle transition exactly once.
 *
 * ```kotlin
 * user.withPendingWithdrawalDecision(amount, currency) { _ ->
 *     PendingExecutionDecision.fromBoolean(tryRenameClan())
 * }
 * ```
 */
sealed interface PendingExecutionDecision<out T> {

    /** The value returned to the caller after committing the financial transaction. */
    val value: T

    /**
     * Requests commit because the external operation succeeded.
     *
     * @property value the external operation result retained in the execution result
     */
    data class Commit<T>(override val value: T) : PendingExecutionDecision<T>

    /**
     * Requests rollback because the external operation reported a known unsuccessful outcome.
     *
     * This is intended for explicit values such as `false`, `NotFound`, or `Rejected`; uncertain
     * outcomes such as a timeout should leave the reservation pending for reconciliation instead.
     *
     * @property value the external operation result retained in the execution result
     */
    data class Rollback<T>(override val value: T) : PendingExecutionDecision<T>

    companion object {
        /** Returns a commit decision carrying [Unit]. */
        fun commit(): PendingExecutionDecision<Unit> = Commit(Unit)

        /** Returns a commit decision carrying [value]. */
        fun <T> commit(value: T): PendingExecutionDecision<T> = Commit(value)

        /** Returns a rollback decision carrying [Unit]. */
        fun rollback(): PendingExecutionDecision<Unit> = Rollback(Unit)

        /** Returns a rollback decision carrying [value]. */
        fun <T> rollback(value: T): PendingExecutionDecision<T> = Rollback(value)

        /**
         * Maps a Boolean success result to a commit or rollback decision while retaining the value.
         *
         * `true` requests commit and `false` requests rollback.
         */
        fun fromBoolean(successful: Boolean): PendingExecutionDecision<Boolean> =
            if (successful) Commit(true) else Rollback(false)

        /**
         * Maps [value] to a decision using [isSuccessful] and retains the value in the result.
         *
         * This is useful for external APIs returning a sealed result or status enum.
         */
        inline fun <T> from(
            value: T,
            isSuccessful: (T) -> Boolean
        ): PendingExecutionDecision<T> =
            if (isSuccessful(value)) Commit(value) else Rollback(value)
    }
}

/**
 * Describes why an automatic commit or rollback did not reach its requested state.
 *
 * Domain-level lifecycle conflicts are represented by [TypedResult]. Exceptions thrown by the RPC
 * call, including reconstructed microservice exceptions and transport failures, are represented by
 * [Thrown]. Cancellation and fatal JVM errors are never wrapped and continue to propagate.
 */
sealed interface PendingLifecycleFailure<out R> {

    /**
     * The lifecycle call returned a typed result that did not represent the requested final state.
     *
     * For example, commit may return `Expired`, or rollback may return `Committed`.
     */
    data class TypedResult<R>(val result: R) : PendingLifecycleFailure<R>

    /**
     * The lifecycle RPC threw before its outcome could be confirmed.
     *
     * The cause may be a reconstructed database exception from the microservice or a client-side
     * transport exception. Reconcile the transaction identifier before choosing another action.
     */
    data class Thrown(val cause: Throwable) : PendingLifecycleFailure<Nothing>
}

/**
 * The result of coordinating an external operation with a pending financial transaction.
 *
 * This is saga-style coordination, not a globally atomic transaction with the external service.
 * A result therefore describes both the external outcome and the separately persisted financial
 * lifecycle transition. Callers should handle every subtype explicitly, especially commit and
 * rollback failures whose final state may require reconciliation.
 */
sealed interface PendingExecutionResult<out T> {

    /**
     * The external operation reported success and the financial transaction reached committed
     * state.
     *
     * This is returned after a normally completed automatic block or a
     * [PendingExecutionDecision.Commit]. An already committed retry is also considered completed.
     *
     * @property value value returned by the external operation
     * @property transaction committed transaction snapshot
     */
    data class Completed<T>(
        val value: T,
        val transaction: Transaction
    ) : PendingExecutionResult<T>

    /**
     * The reservation was not created, so the external operation was never executed.
     *
     * Examples include insufficient sender funds, an invalid timeout, or internally inconsistent
     * transfer data. Database and RPC failures are thrown instead of being reported by this subtype.
     *
     * @property result typed reservation failure
     */
    data class ReservationFailed(
        val result: PendingTransactionResult
    ) : PendingExecutionResult<Nothing>

    /**
     * The external operation returned a known unsuccessful value and the requested financial
     * rollback succeeded.
     *
     * For example, a clan rename API returned `false`, the block selected
     * [PendingExecutionDecision.Rollback], and the reserved price was released. An already rolled
     * back retry is also considered successful.
     *
     * @property value unsuccessful external result that selected rollback
     * @property transaction rolled-back transaction snapshot
     */
    data class RolledBack<T>(
        val value: T,
        val transaction: Transaction
    ) : PendingExecutionResult<T>

    /**
     * The external operation returned a known unsuccessful value, but the requested rollback did
     * not reach rolled-back state.
     *
     * This may occur when the reservation expired or was committed concurrently, or when the
     * rollback RPC threw before its outcome could be confirmed. Inspect [failure] and reconcile
     * using the stable [transaction] identifier; do not report the funds as released merely because
     * rollback was requested.
     *
     * @property value unsuccessful external result that selected rollback
     * @property transaction original pending transaction snapshot
     * @property failure typed rollback conflict or thrown RPC failure
     */
    data class RollbackFailed<T>(
        val value: T,
        val transaction: Transaction,
        val failure: PendingLifecycleFailure<TransactionRollbackResult>
    ) : PendingExecutionResult<T>

    /**
     * A configured exception occurred during the external operation and financial rollback
     * succeeded.
     *
     * For example, `ClanRenameRejectedException` was explicitly included in the rollback policy and
     * therefore proved that the rename produced no external side effect. An already rolled-back
     * retry is also considered successful.
     *
     * @property cause configured external failure
     * @property transaction rolled-back transaction snapshot
     */
    data class ExternalFailureRolledBack(
        val cause: Throwable,
        val transaction: Transaction
    ) : PendingExecutionResult<Nothing>

    /**
     * A configured exception occurred, but financial rollback did not reach rolled-back state.
     *
     * Examples include a thrown rollback RPC, expiration before rollback, or a conflicting
     * concurrent commit. Inspect [failure] and reconcile [transaction] instead of assuming that the
     * reservation was released.
     *
     * @property cause configured external failure
     * @property transaction original pending transaction snapshot
     * @property failure typed rollback conflict or thrown RPC failure
     */
    data class ExternalFailureRollbackFailed(
        val cause: Throwable,
        val transaction: Transaction,
        val failure: PendingLifecycleFailure<TransactionRollbackResult>
    ) : PendingExecutionResult<Nothing>

    /**
     * The external operation succeeded, but commit failed or its outcome is indeterminate.
     *
     * This may occur when the commit RPC response is lost, the reservation expires before commit,
     * or another actor rolls it back concurrently. Because the external side effect already
     * succeeded, this outcome never triggers automatic rollback. Retry or reconcile commit using
     * [transaction] and [failure].
     *
     * @property value value returned by the successful external operation
     * @property transaction pending transaction retained for reconciliation
     * @property failure typed commit conflict or thrown RPC failure
     */
    data class CommitFailed<T>(
        val value: T,
        val transaction: Transaction,
        val failure: PendingLifecycleFailure<TransactionCommitResult>
    ) : PendingExecutionResult<T>
}
