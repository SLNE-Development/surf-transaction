package dev.slne.surf.transaction.core.common.transactional

import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.PendingTransactionResult
import dev.slne.surf.transaction.api.transaction.Transaction
import dev.slne.surf.transaction.api.transaction.TransactionCommitResult
import dev.slne.surf.transaction.api.transaction.TransactionRollbackResult
import dev.slne.surf.transaction.api.transaction.TransactionState
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.transactional.PendingExecutionDecision
import dev.slne.surf.transaction.api.transactional.PendingExecutionResult
import dev.slne.surf.transaction.api.transactional.PendingLifecycleFailure
import dev.slne.surf.transaction.api.transactional.PendingRollbackPolicy
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class PendingExecutionTest {
    @Test
    fun `reservation failure never executes external operation`() = runBlocking {
        var executed = false
        val result = executePendingReservation(
            reservation = PendingTransactionResult.SenderInsufficientFunds,
            rollbackOn = PendingRollbackPolicy.Never,
            block = {
                executed = true
            },
            commit = { error("commit must not run") },
            rollback = { error("rollback must not run") }
        )

        assertEquals(false, executed)
        assertTrue(result is PendingExecutionResult.ReservationFailed)
    }

    @Test
    fun `normal completion commits`() = runBlocking {
        val pending = transaction()
        var commits = 0
        val result = executePendingReservation(
            reservation = PendingTransactionResult.Created(pending),
            rollbackOn = PendingRollbackPolicy.Never,
            block = { "renamed" },
            commit = {
                commits++
                TransactionCommitResult.Committed(pending.copy(state = TransactionState.COMMITTED))
            },
            rollback = { error("rollback must not run") }
        )

        assertEquals(1, commits)
        assertTrue(result is PendingExecutionResult.Completed)
        val completed = result as PendingExecutionResult.Completed
        assertEquals("renamed", completed.value)
    }

    @Test
    fun `transfer execution exposes only sender identifier for correlation`() = runBlocking {
        val sender = transaction()
        val receiver = transaction()
        var blockIdentifier: UUID? = null
        var commitTransaction: Transaction? = null

        val result = executePendingReservation(
            reservation = PendingTransactionResult.TransferCreated(sender, receiver),
            rollbackOn = PendingRollbackPolicy.Never,
            block = {
                blockIdentifier = it
                "transferred"
            },
            commit = {
                commitTransaction = it
                TransactionCommitResult.Committed(
                    sender.copy(state = TransactionState.COMMITTED)
                )
            },
            rollback = { error("rollback must not run") }
        )

        assertEquals(sender.identifier, blockIdentifier)
        assertEquals(sender.identifier, commitTransaction?.identifier)
        assertTrue(result is PendingExecutionResult.Completed)
    }

    @Test
    fun `successful value decision commits`() = runBlocking {
        val pending = transaction()
        var commits = 0
        var rollbacks = 0

        val result = executePendingReservationDecision(
            reservation = PendingTransactionResult.Created(pending),
            rollbackOn = PendingRollbackPolicy.Never,
            block = { PendingExecutionDecision.fromBoolean(true) },
            commit = {
                commits++
                TransactionCommitResult.Committed(
                    pending.copy(state = TransactionState.COMMITTED)
                )
            },
            rollback = {
                rollbacks++
                TransactionRollbackResult.RolledBack(pending)
            }
        )

        assertEquals(1, commits)
        assertEquals(0, rollbacks)
        assertEquals(true, (result as PendingExecutionResult.Completed).value)
    }

    @Test
    fun `unsuccessful value decision rolls back`() = runBlocking {
        val pending = transaction()
        var commits = 0
        var rollbacks = 0

        val result = executePendingReservationDecision(
            reservation = PendingTransactionResult.Created(pending),
            rollbackOn = PendingRollbackPolicy.Never,
            block = { PendingExecutionDecision.fromBoolean(false) },
            commit = {
                commits++
                TransactionCommitResult.Committed(pending)
            },
            rollback = {
                rollbacks++
                TransactionRollbackResult.RolledBack(
                    pending.copy(state = TransactionState.ROLLED_BACK)
                )
            }
        )

        assertEquals(0, commits)
        assertEquals(1, rollbacks)
        assertEquals(false, (result as PendingExecutionResult.RolledBack).value)
    }

    @Test
    fun `unsuccessful value reports rollback failure precisely`() = runBlocking {
        val pending = transaction()

        val result = executePendingReservationDecision(
            reservation = PendingTransactionResult.Created(pending),
            rollbackOn = PendingRollbackPolicy.Never,
            block = { PendingExecutionDecision.rollback("rejected") },
            commit = { error("commit must not run") },
            rollback = {
                TransactionRollbackResult.Expired(
                    pending.copy(state = TransactionState.EXPIRED)
                )
            }
        )

        assertTrue(result is PendingExecutionResult.RollbackFailed)
        assertEquals("rejected", (result as PendingExecutionResult.RollbackFailed).value)
    }

    @Test
    fun `successful action followed by commit failure is never rolled back`() = runBlocking {
        val pending = transaction()
        var rollbacks = 0
        val result = executePendingReservation(
            reservation = PendingTransactionResult.Created(pending),
            rollbackOn = PendingRollbackPolicy { true },
            block = { "external-success" },
            commit = {
                TransactionCommitResult.Expired(pending.copy(state = TransactionState.EXPIRED))
            },
            rollback = {
                rollbacks++
                TransactionRollbackResult.RolledBack(pending)
            }
        )

        assertTrue(result is PendingExecutionResult.CommitFailed)
        val failed = result as PendingExecutionResult.CommitFailed
        assertEquals(pending.identifier, failed.transaction.identifier)
        assertTrue(failed.failure is PendingLifecycleFailure.TypedResult)
        assertEquals(0, rollbacks)
    }

    @Test
    fun `thrown commit failure is returned for reconciliation`() = runBlocking {
        val pending = transaction()
        val failure = IllegalStateException("remote database failure")

        val result = executePendingReservation(
            reservation = PendingTransactionResult.Created(pending),
            rollbackOn = PendingRollbackPolicy.Never,
            block = { "external-success" },
            commit = { throw failure },
            rollback = { error("rollback must not run") }
        )

        assertTrue(result is PendingExecutionResult.CommitFailed)
        val thrown = (result as PendingExecutionResult.CommitFailed).failure
        assertEquals(failure, (thrown as PendingLifecycleFailure.Thrown).cause)
    }

    @Test
    fun `only configured failures trigger rollback`() = runBlocking {
        val pending = transaction()
        var rollbacks = 0
        val result = executePendingReservation(
            reservation = PendingTransactionResult.Created(pending),
            rollbackOn = PendingRollbackPolicy.on<KnownFailure>(),
            block = { throw KnownFailure() },
            commit = { error("commit must not run") },
            rollback = {
                rollbacks++
                TransactionRollbackResult.RolledBack(
                    pending.copy(state = TransactionState.ROLLED_BACK)
                )
            }
        )

        assertEquals(1, rollbacks)
        assertTrue(result is PendingExecutionResult.ExternalFailureRolledBack)
    }

    @Test
    fun `unmatched failure is propagated and leaves reservation pending`() = runBlocking {
        val pending = transaction()
        var rollbacks = 0

        assertThrows(UnknownFailure::class.java) {
            runBlocking {
                executePendingReservation(
                    reservation = PendingTransactionResult.Created(pending),
                    rollbackOn = PendingRollbackPolicy.on<KnownFailure>(),
                    block = { throw UnknownFailure() },
                    commit = { error("commit must not run") },
                    rollback = {
                        rollbacks++
                        TransactionRollbackResult.RolledBack(pending)
                    }
                )
            }
        }
        assertEquals(0, rollbacks)
    }

    @Test
    fun `cancellation is propagated and leaves reservation pending`() = runBlocking {
        val pending = transaction()
        var rollbacks = 0

        assertThrows(CancellationException::class.java) {
            runBlocking {
                executePendingReservation(
                    reservation = PendingTransactionResult.Created(pending),
                    rollbackOn = PendingRollbackPolicy { true },
                    block = { throw CancellationException("cancelled") },
                    commit = { error("commit must not run") },
                    rollback = {
                        rollbacks++
                        TransactionRollbackResult.RolledBack(pending)
                    }
                )
            }
        }
        assertEquals(0, rollbacks)
    }

    @Test
    fun `rollback failure is reported precisely`() = runBlocking {
        val pending = transaction()
        val result = executePendingReservation(
            reservation = PendingTransactionResult.Created(pending),
            rollbackOn = PendingRollbackPolicy.on<KnownFailure>(),
            block = { throw KnownFailure() },
            commit = { error("commit must not run") },
            rollback = {
                TransactionRollbackResult.Committed(
                    pending.copy(state = TransactionState.COMMITTED)
                )
            }
        )

        assertTrue(result is PendingExecutionResult.ExternalFailureRollbackFailed)
        assertTrue(
            (result as PendingExecutionResult.ExternalFailureRollbackFailed).failure is
                    PendingLifecycleFailure.TypedResult
        )
    }

    @Test
    fun `thrown rollback failure retains the original external cause`() = runBlocking {
        val pending = transaction()
        val rollbackFailure = IllegalStateException("remote rollback failure")

        val result = executePendingReservation(
            reservation = PendingTransactionResult.Created(pending),
            rollbackOn = PendingRollbackPolicy.on<KnownFailure>(),
            block = { throw KnownFailure() },
            commit = { error("commit must not run") },
            rollback = { throw rollbackFailure }
        )

        val failed = result as PendingExecutionResult.ExternalFailureRollbackFailed
        assertTrue(failed.cause is KnownFailure)
        assertEquals(
            rollbackFailure,
            (failed.failure as PendingLifecycleFailure.Thrown).cause
        )
    }

    private fun transaction() = TestTransaction(
        identifier = UUID.randomUUID(),
        state = TransactionState.PENDING,
        expiresAt = Instant.now().plusSeconds(30)
    )

    private class KnownFailure : RuntimeException()
    private class UnknownFailure : RuntimeException()

    private data class TestTransaction(
        override val identifier: UUID,
        override val state: TransactionState,
        override val expiresAt: Instant?
    ) : Transaction {
        override val initiator: UUID? = null
        override val senderAccountId: UUID? = null
        override val receiverAccountId: UUID? = null
        override val currency: Currency
            get() = error("not used")
        override val amount: BigDecimal = BigDecimal.TEN.negate()
        override val ignoreMinimumAmount: Boolean = false
        override val data: Set<TransactionData> = emptySet()

        override suspend fun receiverAccount(): Account? = null
        override suspend fun senderAccount(): Account? = null
        override suspend fun commit(): TransactionCommitResult = error("not used")
        override suspend fun rollback(): TransactionRollbackResult = error("not used")
        override suspend fun refresh() = error("not used")
    }
}
