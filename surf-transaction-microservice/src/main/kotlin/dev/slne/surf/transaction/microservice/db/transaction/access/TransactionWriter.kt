package dev.slne.surf.transaction.microservice.db.transaction.access

import dev.slne.surf.database.libs.io.r2dbc.spi.R2dbcDataIntegrityViolationException
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.SortOrder
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.and
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.dao.id.EntityID
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.inList
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.statements.BatchInsertStatement
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.javatime.CurrentTimestamp
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.ExposedR2dbcException
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.batchInsert
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.select
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.database.utils.asDataIntegrityViolation
import dev.slne.surf.transaction.api.transaction.TransactionState
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.core.common.transaction.TransactionImpl
import dev.slne.surf.transaction.microservice.db.account.AccountTable
import dev.slne.surf.transaction.microservice.db.currency.CurrencyTable
import dev.slne.surf.transaction.microservice.db.transaction.access.TransactionWriteResult.UnknownAccountOrCurrency
import dev.slne.surf.transaction.microservice.db.transaction.support.TransactionRules
import dev.slne.surf.transaction.microservice.db.transaction.table.TransactionDataTable
import dev.slne.surf.transaction.microservice.db.transaction.table.TransactionTable
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import java.io.Serial
import java.math.BigDecimal
import java.time.Instant
import java.util.*

/**
 * Persists new transactions.
 *
 * One call writes one logical operation: a single transaction, or both sides of a transfer as
 * one atomic unit. All rows of a call are inserted inside one database transaction, share one
 * operation id, and are rolled back together on any failure. Duplicate identifiers are
 * resolved idempotently: retrying an identical write returns the already stored result instead
 * of failing.
 */
internal class TransactionWriter(private val reader: TransactionReader) {

    suspend fun writeCommitted(transactions: List<TransactionImpl>): TransactionWriteResult =
        write(transactions, TransactionState.COMMITTED, timeoutMillis = null)

    /** Writes pending reservations that expire [timeoutMillis] after the database clock's now. */
    suspend fun writePending(
        transactions: List<TransactionImpl>,
        timeoutMillis: Long
    ): TransactionWriteResult = write(transactions, TransactionState.PENDING, timeoutMillis)

    private suspend fun write(
        transactions: List<TransactionImpl>,
        state: TransactionState,
        timeoutMillis: Long?
    ): TransactionWriteResult = try {
        suspendTransaction {
            insertAll(transactions, state, timeoutMillis)
        }
    } catch (_: MinimumBalanceViolation) {
        TransactionWriteResult.InsufficientFunds
    } catch (cause: ExposedR2dbcException) {
        resolveDuplicateIdentifiers(transactions, state, cause.asDataIntegrityViolation())
    }

    /**
     * The row inserts intentionally precede the minimum-balance check. This lets the unique
     * identifier constraint recognize an idempotent retry before an already existing reservation
     * could be counted against the balance twice. A minimum violation is raised as an exception so
     * the surrounding database transaction rolls back every inserted row.
     */
    private suspend fun insertAll(
        transactions: List<TransactionImpl>,
        state: TransactionState,
        timeoutMillis: Long?
    ): TransactionWriteResult {
        require(transactions.isNotEmpty()) { "Cannot write an empty transaction list" }

        val context = writeContext(transactions) ?: return UnknownAccountOrCurrency
        val expiresAt = timeoutMillis?.let(context.now::plusMillis)
        val operationId = transactions.first().identifier

        val inserted = transactions.map { transaction ->
            val row = insertRow(transaction, context, state, expiresAt, operationId)
            if (violatesMinimumBalance(transaction, context)) throw MinimumBalanceViolation()
            row
        }
        insertDataRows(inserted)
        return TransactionWriteResult.Written(inserted.map(InsertedRow::transaction))
    }

    /**
     * Decides what a unique-constraint violation for [attempted] means. If the stored rows
     * describe exactly the attempted write (same state and financial data, transfer sides linked),
     * the write is a retry and the stored snapshots are returned as success. The stored expiry is
     * deliberately not compared: a retried reservation keeps its original expiry.
     */
    private suspend fun resolveDuplicateIdentifiers(
        attempted: List<TransactionImpl>,
        expectedState: TransactionState,
        violation: R2dbcDataIntegrityViolationException
    ): TransactionWriteResult = suspendTransaction {
        val storedById = reader.findAll(
            attempted.mapTo(ObjectOpenHashSet(attempted.size)) { it.identifier }
        )

        if (storedById.isEmpty()) {
            // None of our identifiers exist, so some other constraint failed.
            return@suspendTransaction TransactionWriteResult.ConstraintViolation(violation)
        }

        val stored = attempted.mapNotNull { storedById[it.identifier] }
        val snapshots = stored.map(StoredTransaction::currentSnapshot)

        val isRetryOfSameWrite = snapshots.matchAttemptedWrite(attempted, expectedState)
                && stored.belongsToSingleOperation()

        if (isRetryOfSameWrite) {
            TransactionWriteResult.Written(snapshots)
        } else {
            TransactionWriteResult.IdentifierConflict(attempted.first().identifier)
        }
    }

    private fun List<TransactionImpl>.matchAttemptedWrite(
        attempted: List<TransactionImpl>,
        expectedState: TransactionState
    ): Boolean {
        if (size != attempted.size) return false

        return indices.all { index ->
            val snapshot = this[index]
            val attempt = attempted[index]

            snapshot.state == expectedState && snapshot.sameWriteDataAs(attempt)
        }
    }

    /**
     * Returns whether all stored rows belong to the same logical operation.
     *
     * A single transaction does not require an operation id because legacy standalone rows may not
     * have one.
     */
    private fun List<StoredTransaction>.belongsToSingleOperation(): Boolean {
        if (size < 2) return true

        val operationId = first().operationId ?: return false
        return all { it.operationId == operationId }
    }

    /**
     * Resolves the account and currency rows referenced by [transactions] plus the database time.
     * Returns null when a reference does not exist, the transactions mix currencies, or no account
     * is involved at all.
     *
     * When any transaction needs a minimum-balance check, the account rows are locked
     * (`FOR UPDATE`) so concurrent reservations against the same account are serialized and cannot
     * overspend past the currency minimum.
     */
    private suspend fun writeContext(transactions: List<TransactionImpl>): WriteContext? {
        val currencyName = transactions.commonCurrencyName() ?: return null
        val accountIds = transactions.referencedAccountIds()

        if (accountIds.isEmpty()) return null

        return if (transactions.any(TransactionImpl::requiresMinimumBalanceCheck)) {
            lockingWriteContext(accountIds, currencyName)
        } else {
            plainWriteContext(accountIds, currencyName)
        }
    }

    private fun List<TransactionImpl>.commonCurrencyName(): String? {
        val currencyName = firstOrNull()?.currencyName ?: return null

        return if (all { it.currencyName == currencyName }) {
            currencyName
        } else {
            null
        }
    }

    private fun List<TransactionImpl>.referencedAccountIds(): Set<UUID> {
        val accountIds = ObjectOpenHashSet<UUID>(size * 2)

        for (transaction in this) {
            transaction.senderAccountId?.let(accountIds::add)
            transaction.receiverAccountId?.let(accountIds::add)
        }

        return accountIds
    }

    private suspend fun lockingWriteContext(
        accountIds: Set<UUID>,
        currencyName: String
    ): WriteContext? {
        // Lock in a stable order to avoid deadlocks between concurrent transfers
        val accountRows = AccountTable
            .select(AccountTable.id, AccountTable.accountId, CurrentTimestamp)
            .where { AccountTable.accountId inList accountIds }
            .orderBy(AccountTable.id, SortOrder.ASC)
            .forUpdate()
            .toList()
        if (accountRows.size != accountIds.size) return null

        val currencyRow = CurrencyTable
            .select(CurrencyTable.id, CurrencyTable.minimumAmount)
            .where { CurrencyTable.name eq currencyName }
            .limit(1)
            .singleOrNull()
            ?: return null

        return WriteContext(
            accounts = accountRows.associate {
                it[AccountTable.accountId] to it[AccountTable.id]
            },
            currencyId = currencyRow[CurrencyTable.id],
            minimumAmount = currencyRow[CurrencyTable.minimumAmount],
            now = accountRows.first()[CurrentTimestamp]
        )
    }

    private suspend fun plainWriteContext(
        accountIds: Set<UUID>,
        currencyName: String
    ): WriteContext? {
        val rows = AccountTable
            .crossJoin(CurrencyTable)
            .select(
                AccountTable.id,
                AccountTable.accountId,
                CurrencyTable.id,
                CurrentTimestamp
            )
            .where {
                (AccountTable.accountId inList accountIds) and
                        (CurrencyTable.name eq currencyName)
            }
            .orderBy(AccountTable.id, SortOrder.ASC)
            .toList()

        if (rows.size != accountIds.size) return null

        val firstRow = rows.first()
        return WriteContext(
            accounts = rows.associate {
                it[AccountTable.accountId] to it[AccountTable.id]
            },
            currencyId = firstRow[CurrencyTable.id],
            // No minimum-balance check is needed, so the currency's minimum is irrelevant
            minimumAmount = BigDecimal.ZERO,
            now = firstRow[CurrentTimestamp]
        )
    }

    private suspend fun insertRow(
        transaction: TransactionImpl,
        context: WriteContext,
        state: TransactionState,
        expiresAt: Instant?,
        operationId: UUID
    ): InsertedRow {
        val databaseId = TransactionTable.insertAndGetId {
            it[identifier] = transaction.identifier
            it[initiator] = transaction.initiator
            it[amount] = transaction.amount
            it[currency] = context.currencyId
            it[ignoreMinimumAmount] = transaction.ignoreMinimumAmount
            it[TransactionTable.state] = state
            it[TransactionTable.expiresAt] = expiresAt
            it[TransactionTable.operationId] = operationId
            transaction.senderAccountId?.let { accountId ->
                it[sender] = context.accounts.getValue(accountId)
            }
            transaction.receiverAccountId?.let { accountId ->
                it[receiver] = context.accounts.getValue(accountId)
            }
        }.value

        return InsertedRow(
            databaseId = databaseId,
            transaction = transaction.copy(state = state, expiresAt = expiresAt)
        )
    }

    private suspend fun insertDataRows(inserted: List<InsertedRow>) {
        val entries = inserted.flatMap { row ->
            row.transaction.data.map { data ->
                row.databaseId to data
            }
        }

        if (entries.isEmpty()) return

        TransactionDataTable.batchInsert(
            entries,
            shouldReturnGeneratedValues = false
        ) { (transactionId, data) ->
            insertTransactionData(transactionId, data)
        }
    }

    /**
     * Whether the row just inserted for [transaction] pushed its account below the currency
     * minimum. The row is already part of the balance, so no additional amount is applied.
     */
    private suspend fun violatesMinimumBalance(
        transaction: TransactionImpl,
        context: WriteContext
    ): Boolean {
        if (!transaction.requiresMinimumBalanceCheck()) return false

        val accountId = transaction.receiverAccountId?.let { context.accounts[it] } ?: return false
        val availableBalance = reader.availableBalance(
            accountId = accountId,
            currencyId = context.currencyId,
            now = context.now
        )
        return !TransactionRules.remainsAtOrAboveMinimum(
            availableBalance = availableBalance,
            amount = BigDecimal.ZERO,
            minimumAmount = context.minimumAmount
        )
    }

    private fun BatchInsertStatement.insertTransactionData(
        transactionId: ULong,
        data: TransactionData
    ) {
        this[TransactionDataTable.transaction] = transactionId
        this[TransactionDataTable.key] = data.key
        this[TransactionDataTable.value] = data.value
    }

    /** Control-flow exception used to roll back the database transaction; no stack trace needed. */
    private class MinimumBalanceViolation : RuntimeException(null, null, false, false) {
        companion object {
            @Serial
            private const val serialVersionUID: Long = 529853184731657606L
        }
    }
}

/** Outcome of a write attempt, mapped to the API result types by the repository. */
internal sealed interface TransactionWriteResult {

    /** All rows are durable; [transactions] are the stored snapshots in input order. */
    data class Written(val transactions: List<TransactionImpl>) : TransactionWriteResult

    /** The debited account would fall below the currency minimum; nothing was written. */
    data object InsufficientFunds : TransactionWriteResult

    /** A referenced account or currency does not exist. */
    data object UnknownAccountOrCurrency : TransactionWriteResult

    /** [identifier] already belongs to a transaction with different data or state. */
    data class IdentifierConflict(val identifier: UUID) : TransactionWriteResult

    /** A database constraint other than the identifier uniqueness failed. */
    data class ConstraintViolation(
        val violation: R2dbcDataIntegrityViolationException
    ) : TransactionWriteResult
}

private data class WriteContext(
    val accounts: Map<UUID, EntityID<ULong>>,
    val currencyId: EntityID<ULong>,
    val minimumAmount: BigDecimal,
    val now: Instant
)

private data class InsertedRow(
    val databaseId: ULong,
    val transaction: TransactionImpl
)

/**
 * Returns whether this transaction requires a minimum-balance check.
 *
 * The check only applies to withdrawals: entries that debit an account and do not explicitly
 * opt out of minimum-balance validation.
 */
private fun TransactionImpl.requiresMinimumBalanceCheck(): Boolean {
    return !ignoreMinimumAmount &&
            amount.signum() < 0 &&
            receiverAccountId != null
}

private fun TransactionImpl.sameWriteDataAs(other: TransactionImpl): Boolean =
    identifier == other.identifier &&
            initiator == other.initiator &&
            senderAccountId == other.senderAccountId &&
            receiverAccountId == other.receiverAccountId &&
            currencyName == other.currencyName &&
            amount.compareTo(other.amount) == 0 &&
            data == other.data &&
            ignoreMinimumAmount == other.ignoreMinimumAmount

private fun TransactionImpl.hasSameIdentityAs(other: TransactionImpl): Boolean {
    return identifier == other.identifier && initiator == other.initiator
}

private fun TransactionImpl.hasSameRoutingAs(other: TransactionImpl): Boolean {
    return senderAccountId == other.senderAccountId &&
            receiverAccountId == other.receiverAccountId &&
            currencyName == other.currencyName
}
