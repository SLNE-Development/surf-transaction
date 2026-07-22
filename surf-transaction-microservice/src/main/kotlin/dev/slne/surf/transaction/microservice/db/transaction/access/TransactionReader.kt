package dev.slne.surf.transaction.microservice.db.transaction.access

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.*
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.dao.id.EntityID
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.javatime.CurrentTimestamp
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.select
import dev.slne.surf.transaction.api.transaction.TransactionState
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.core.common.transaction.TransactionImpl
import dev.slne.surf.transaction.microservice.db.account.AccountTable
import dev.slne.surf.transaction.microservice.db.currency.CurrencyTable
import dev.slne.surf.transaction.microservice.db.transaction.support.TransactionRules
import dev.slne.surf.transaction.microservice.db.transaction.table.TransactionDataTable
import dev.slne.surf.transaction.microservice.db.transaction.table.TransactionTable
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import java.math.BigDecimal
import java.time.Instant
import java.util.*

/**
 * Reads stored transaction snapshots and available balances.
 *
 * Every function issues plain queries and expects the caller to provide the surrounding database
 * transaction.
 */
internal class TransactionReader {
    private val senderAccount = AccountTable.alias("transaction_sender_account")
    private val receiverAccount = AccountTable.alias("transaction_receiver_account")

    private val transactionWithDetails = TransactionTable
        .innerJoin(CurrencyTable)
        .leftJoin(
            senderAccount,
            { TransactionTable.sender },
            { senderAccount[AccountTable.id] }
        )
        .leftJoin(
            receiverAccount,
            { TransactionTable.receiver },
            { receiverAccount[AccountTable.id] }
        )
        .leftJoin(TransactionDataTable)

    private val transactionWithReceiverAndCurrency = TransactionTable
        .innerJoin(
            AccountTable,
            { TransactionTable.receiver },
            { AccountTable.id }
        )
        .innerJoin(CurrencyTable)

    suspend fun find(identifier: UUID): StoredTransaction? {
        return findAll(setOf(identifier))[identifier]
    }

    suspend fun findAll(identifiers: Set<UUID>): Map<UUID, StoredTransaction> {
        if (identifiers.isEmpty()) return emptyMap()

        val rows = transactionWithDetails
            .select(
                TransactionTable.identifier,
                TransactionTable.initiator,
                TransactionTable.amount,
                TransactionTable.ignoreMinimumAmount,
                TransactionTable.state,
                TransactionTable.expiresAt,
                TransactionTable.operationId,
                CurrencyTable.name,
                senderAccount[AccountTable.accountId],
                receiverAccount[AccountTable.accountId],
                TransactionDataTable.key,
                TransactionDataTable.value,
                CurrentTimestamp
            )
            .where { TransactionTable.identifier inList identifiers }
            .toList()

        return rows
            .groupBy { it[TransactionTable.identifier] }
            .mapValues { (_, transactionRows) -> storedTransaction(transactionRows) }
    }

    /** Available balance of [accountId] in [currencyName], evaluated against the database clock. */
    suspend fun availableBalance(accountId: UUID, currencyName: String): BigDecimal {
        val selector = listOf(
            AccountTable.accountId eq accountId,
            CurrencyTable.name eq currencyName,
            TransactionRules.availableBalancePredicate(CurrentTimestamp)
        ).compoundAnd()

        return transactionWithReceiverAndCurrency.sumAvailableAmount(selector)
    }

    /**
     * [availableBalance] variant for the write path, where account and currency ids are already
     * resolved and [now] was read from the database together with the row locks.
     */
    suspend fun availableBalance(
        accountId: EntityID<ULong>,
        currencyId: EntityID<ULong>,
        now: Instant
    ): BigDecimal {
        val selector = listOf(
            TransactionTable.receiver eq accountId,
            TransactionTable.currency eq currencyId,
            TransactionRules.availableBalancePredicate(now)
        ).compoundAnd()

        return TransactionTable.sumAvailableAmount(selector)
    }

    /**
     * Sums the amounts of all transaction rows matching [selector].
     *
     * Returns zero when no matching transaction contributes an amount.
     */
    private suspend fun ColumnSet.sumAvailableAmount(
        selector: Op<Boolean>
    ): BigDecimal {
        val totalAmount = TransactionTable.amount.sum()

        return select(totalAmount)
            .where(selector)
            .map { it[totalAmount] }
            .singleOrNull()
            ?: BigDecimal.ZERO
    }

    private fun storedTransaction(rows: List<ResultRow>): StoredTransaction {
        val transactionRow = rows.first()
        val data = ObjectOpenHashSet<TransactionData>()

        for (row in rows) {
            val key = row.getOrNull(TransactionDataTable.key) ?: continue
            val value = row.getOrNull(TransactionDataTable.value) ?: continue
            data.add(TransactionData.of(key, value))
        }

        return StoredTransaction(
            operationId = transactionRow[TransactionTable.operationId],
            observedAt = transactionRow[CurrentTimestamp],
            transaction = TransactionImpl(
                identifier = transactionRow[TransactionTable.identifier],
                initiator = transactionRow[TransactionTable.initiator],
                senderAccountId = transactionRow.getOrNull(senderAccount[AccountTable.accountId]),
                receiverAccountId = transactionRow.getOrNull(receiverAccount[AccountTable.accountId]),
                currencyName = transactionRow[CurrencyTable.name],
                amount = transactionRow[TransactionTable.amount],
                data = data,
                ignoreMinimumAmount = transactionRow[TransactionTable.ignoreMinimumAmount],
                state = transactionRow[TransactionTable.state],
                expiresAt = transactionRow[TransactionTable.expiresAt]
            )
        )
    }
}

/**
 * A transaction as stored in the database, together with the database time at which it was read.
 *
 * [operationId] links the two sides of a transfer so they commit, roll back, or expire together;
 * legacy rows may not carry one.
 */
internal data class StoredTransaction(
    val operationId: UUID?,
    val observedAt: Instant,
    val transaction: TransactionImpl
) {

    /**
     * Returns the transaction state effective at [observedAt].
     *
     * A pending transaction whose expiration time had already been reached is represented as
     * [TransactionState.EXPIRED], even if that state had not yet been persisted by the cleanup
     * job.
     */
    fun currentSnapshot(): TransactionImpl {
        val logicallyExpired = transaction.state == TransactionState.PENDING
                && transaction.expiresAt?.isAfter(observedAt) != true

        return if (logicallyExpired) {
            transaction.copy(state = TransactionState.EXPIRED)
        } else {
            transaction
        }
    }
}
