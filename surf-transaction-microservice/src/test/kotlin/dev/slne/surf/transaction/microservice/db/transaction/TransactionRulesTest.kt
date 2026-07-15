package dev.slne.surf.transaction.microservice.db.transaction

import dev.slne.surf.database.libs.io.r2dbc.spi.R2dbcDataIntegrityViolationException
import dev.slne.surf.transaction.api.transaction.TransactionCommitResult
import dev.slne.surf.transaction.api.transaction.TransactionRollbackResult
import dev.slne.surf.transaction.api.transaction.TransactionState
import dev.slne.surf.transaction.core.common.transaction.TransactionImpl
import dev.slne.surf.transaction.microservice.db.transaction.access.StoredTransaction
import dev.slne.surf.transaction.microservice.db.transaction.support.TransactionErrors
import dev.slne.surf.transaction.microservice.db.transaction.support.TransactionRules
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class TransactionRulesTest {
    private val now: Instant = Instant.parse("2026-07-14T12:00:00Z")

    @Test
    fun `pending withdrawal reserves funds and commit keeps deduction`() {
        val history = AccountHistory(committed("100"), pending("-60"))

        assertAmount("40", history.available(now))
        history.entries[1] = history.entries[1].copy(state = TransactionState.COMMITTED)
        assertAmount("40", history.available(now))
    }

    @Test
    fun `rollback and expiration release a withdrawal reservation`() {
        val rolledBack = AccountHistory(
            committed("100"),
            pending("-60").copy(state = TransactionState.ROLLED_BACK)
        )
        val expiredByState = AccountHistory(
            committed("100"),
            pending("-60").copy(state = TransactionState.EXPIRED)
        )
        val expiredByClock = AccountHistory(
            committed("100"),
            pending("-60", expiresAt = now.minusSeconds(1))
        )

        assertAmount("100", rolledBack.available(now))
        assertAmount("100", expiredByState.available(now))
        assertAmount("100", expiredByClock.available(now))
    }

    @Test
    fun `snapshot exposes effective expiration without waiting for cleanup`() {
        val identifier = UUID.randomUUID()
        val transaction = TransactionImpl(
            identifier = identifier,
            initiator = null,
            senderAccountId = null,
            receiverAccountId = UUID.randomUUID(),
            currencyName = "coins",
            amount = BigDecimal.TEN.negate(),
            data = emptySet(),
            state = TransactionState.PENDING,
            expiresAt = now.minusSeconds(1)
        )
        val stored = StoredTransaction(
            operationId = identifier,
            observedAt = now,
            transaction = transaction
        )

        assertEquals(TransactionState.EXPIRED, stored.currentSnapshot().state)
        assertEquals(TransactionState.PENDING, transaction.state)
    }

    @Test
    fun `constraint failures retain MariaDB and PostgreSQL SQL states`() {
        val violations = listOf(
            R2dbcDataIntegrityViolationException("duplicate", "23000", 1062),
            R2dbcDataIntegrityViolationException("duplicate", "23505", 0)
        )

        violations.forEach { violation ->
            val error = TransactionErrors.constraintViolation(violation)
            val errorText = error.toString()

            assertTrue(errorText.contains("TRANSACTION_CONSTRAINT_VIOLATION"))
            assertTrue(errorText.contains(violation.sqlState.orEmpty()))
        }
    }

    @Test
    fun `pending deposit is unavailable until commit`() {
        val history = AccountHistory(committed("10"), pending("25"))

        assertAmount("10", history.available(now))
        history.entries[1] = history.entries[1].copy(state = TransactionState.COMMITTED)
        assertAmount("35", history.available(now))
    }

    @Test
    fun `pending transfer reserves sender without crediting receiver`() {
        val sender = AccountHistory(committed("100"), pending("-40"))
        val receiver = AccountHistory(pending("40"))

        assertAmount("60", sender.available(now))
        assertAmount("0", receiver.available(now))
    }

    @Test
    fun `transfer commit updates both sides and rollback updates neither`() {
        val committedSender = AccountHistory(committed("100"), committed("-40"))
        val committedReceiver = AccountHistory(committed("40"))
        val rolledBackSender = AccountHistory(
            committed("100"),
            pending("-40").copy(state = TransactionState.ROLLED_BACK)
        )
        val rolledBackReceiver = AccountHistory(
            pending("40").copy(state = TransactionState.ROLLED_BACK)
        )

        assertAmount("60", committedSender.available(now))
        assertAmount("40", committedReceiver.available(now))
        assertAmount("100", rolledBackSender.available(now))
        assertAmount("0", rolledBackReceiver.available(now))
    }

    @Test
    fun `serialized reservation model prevents concurrent overspending`() = runBlocking {
        val history = LockedReservationAccount(BigDecimal("100"), BigDecimal.ZERO, now)

        val results = listOf(
            async(Dispatchers.Default) { history.reserve(BigDecimal("80")) },
            async(Dispatchers.Default) { history.reserve(BigDecimal("80")) }
        ).awaitAll()

        assertEquals(1, results.count { it })
        assertEquals(1, results.count { !it })
        assertAmount("20", history.available())
    }

    @Test
    fun `commit after rollback and commit after expiration are rejected`() {
        assertTrue(
            TransactionRules.commitOutcome(transaction(TransactionState.ROLLED_BACK))
                    is TransactionCommitResult.RolledBack
        )
        assertTrue(
            TransactionRules.commitOutcome(transaction(TransactionState.EXPIRED))
                    is TransactionCommitResult.Expired
        )
    }

    @Test
    fun `rollback after commit is rejected`() {
        assertTrue(
            TransactionRules.rollbackOutcome(transaction(TransactionState.COMMITTED))
                    is TransactionRollbackResult.Committed
        )
    }

    @Test
    fun `repeated terminal transitions have deterministic results`() {
        assertTrue(
            TransactionRules.commitOutcome(transaction(TransactionState.COMMITTED))
                    is TransactionCommitResult.AlreadyCommitted
        )
        assertTrue(
            TransactionRules.rollbackOutcome(transaction(TransactionState.ROLLED_BACK))
                    is TransactionRollbackResult.AlreadyRolledBack
        )
    }

    @Test
    fun `minimum check includes active reservations`() {
        assertTrue(
            TransactionRules.remainsAtOrAboveMinimum(
                availableBalance = BigDecimal("40"),
                amount = BigDecimal("-40"),
                minimumAmount = BigDecimal.ZERO
            )
        )
        assertFalse(
            TransactionRules.remainsAtOrAboveMinimum(
                availableBalance = BigDecimal("40"),
                amount = BigDecimal("-41"),
                minimumAmount = BigDecimal.ZERO
            )
        )
    }

    @Test
    fun `transfer sides must describe one mirrored financial operation`() {
        val senderAccountId = UUID.randomUUID()
        val receiverAccountId = UUID.randomUUID()
        val initiator = UUID.randomUUID()
        val senderTransaction = transferTransaction(
            initiator = initiator,
            senderAccountId = receiverAccountId,
            receiverAccountId = senderAccountId,
            amount = "-25"
        )
        val receiverTransaction = transferTransaction(
            initiator = initiator,
            senderAccountId = senderAccountId,
            receiverAccountId = receiverAccountId,
            amount = "25"
        )

        assertTrue(
            TransactionRules.isMirroredTransferPair(
                senderTransaction,
                receiverTransaction
            )
        )
        assertFalse(
            TransactionRules.isMirroredTransferPair(
                senderTransaction,
                receiverTransaction.copy(amount = BigDecimal("24"))
            )
        )
        assertFalse(
            TransactionRules.isMirroredTransferPair(
                senderTransaction,
                receiverTransaction.copy(currencyName = "gems")
            )
        )
        assertFalse(
            TransactionRules.isMirroredTransferPair(
                senderTransaction,
                receiverTransaction.copy(identifier = senderTransaction.identifier)
            )
        )
    }

    @Test
    fun `legacy transaction snapshots default to committed`() {
        val transaction = TransactionImpl(
            identifier = UUID.randomUUID(),
            initiator = null,
            senderAccountId = null,
            receiverAccountId = UUID.randomUUID(),
            currencyName = "coins",
            amount = BigDecimal.TEN,
            data = emptySet()
        )

        assertEquals(TransactionState.COMMITTED, transaction.state)
        assertEquals(null, transaction.expiresAt)
    }

    private fun committed(amount: String) = Entry(
        amount = BigDecimal(amount),
        state = TransactionState.COMMITTED,
        expiresAt = null
    )

    private fun pending(
        amount: String,
        expiresAt: Instant = now.plusSeconds(30)
    ) = Entry(
        amount = BigDecimal(amount),
        state = TransactionState.PENDING,
        expiresAt = expiresAt
    )

    private fun transaction(state: TransactionState) = TransactionImpl(
        identifier = UUID.randomUUID(),
        initiator = null,
        senderAccountId = null,
        receiverAccountId = UUID.randomUUID(),
        currencyName = "coins",
        amount = BigDecimal.TEN,
        data = emptySet(),
        state = state
    )

    private fun transferTransaction(
        initiator: UUID,
        senderAccountId: UUID,
        receiverAccountId: UUID,
        amount: String
    ) = TransactionImpl(
        identifier = UUID.randomUUID(),
        initiator = initiator,
        senderAccountId = senderAccountId,
        receiverAccountId = receiverAccountId,
        currencyName = "coins",
        amount = BigDecimal(amount),
        data = emptySet()
    )

    private fun assertAmount(expected: String, actual: BigDecimal) {
        assertEquals(0, BigDecimal(expected).compareTo(actual))
    }

    private data class Entry(
        val amount: BigDecimal,
        val state: TransactionState,
        val expiresAt: Instant?
    )

    private class AccountHistory(vararg entries: Entry) {
        val entries = entries.toMutableList()

        fun available(now: Instant): BigDecimal = entries
            .filter {
                TransactionRules.contributesToAvailableBalance(
                    it.state,
                    it.amount,
                    it.expiresAt,
                    now
                )
            }
            .fold(BigDecimal.ZERO) { balance, entry -> balance + entry.amount }
    }

    private class LockedReservationAccount(
        initialBalance: BigDecimal,
        private val minimum: BigDecimal,
        private val now: Instant
    ) {
        private val lock = Mutex()
        private val history = AccountHistory(
            Entry(initialBalance, TransactionState.COMMITTED, null)
        )

        suspend fun reserve(amount: BigDecimal): Boolean = lock.withLock {
            val available = history.available(now)
            val reservation = amount.abs().negate()
            if (!TransactionRules.remainsAtOrAboveMinimum(
                    available,
                    reservation,
                    minimum
                )
            ) {
                return@withLock false
            }
            history.entries += Entry(
                reservation,
                TransactionState.PENDING,
                now.plusSeconds(30)
            )
            true
        }

        fun available(): BigDecimal = history.available(now)
    }
}
