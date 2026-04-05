package dev.slne.surf.transaction.microservice.db.account

import dev.slne.surf.api.core.util.mutableObjectSetOf
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.*
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.*
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.transaction.api.account.member.results.AccountMemberResult
import dev.slne.surf.transaction.core.common.account.AccountImpl
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import java.util.*

class AccountRepositoryImpl : AccountRepository {

    override fun findAccountIDByIdQuery(accountId: UUID): Query =
        AccountTable.select(AccountTable.id)
            .where { AccountTable.accountId eq accountId }
            .limit(1)

    override suspend fun findAccountByAccountId(
        accountId: UUID
    ): AccountImpl? = suspendTransaction {
        findAccountBy { AccountTable.accountId eq accountId }
    }

    override suspend fun findAccountByName(name: String): AccountImpl? = suspendTransaction {
        findAccountBy { AccountTable.name eq name }
    }

    private suspend inline fun findAccountBy(noinline predicate: () -> Op<Boolean>): AccountImpl? {
        val accountResult = AccountTable
            .leftJoin(AccountMemberTable, { AccountTable.id }, { AccountMemberTable.accountId })
            .select(
                AccountTable.ownerId,
                AccountTable.accountId,
                AccountTable.name,
                AccountTable.defaultAccount,
                AccountMemberTable.memberId
            )
            .where(predicate)
            .toList()

        if (accountResult.isEmpty()) return null

        return fromResultRowWithMembers(accountResult)
    }

    override suspend fun findAccountsByOwner(
        ownerUuid: UUID
    ): Set<AccountImpl> = suspendTransaction {
        val accountsResult = AccountTable
            .leftJoin(AccountMemberTable, { AccountTable.id }, { AccountMemberTable.accountId })
            .select(
                AccountTable.ownerId,
                AccountTable.accountId,
                AccountTable.name,
                AccountTable.defaultAccount,
                AccountMemberTable.memberId
            )
            .where { AccountTable.ownerId eq ownerUuid }
            .toList()

        fromMultiResultRowWithMembers(accountsResult)
    }

    override suspend fun existsByAccountName(name: String): Boolean = suspendTransaction {
        AccountTable.select(AccountTable.id)
            .where { AccountTable.name eq name }
            .limit(1)
            .singleOrNull() != null
    }

    override suspend fun createAccount(
        ownerId: UUID,
        name: String,
        defaultAccount: Boolean
    ): AccountImpl = suspendTransaction {
        createAccount0(ownerId, name, defaultAccount)
    }

    private suspend fun createAccount0(
        ownerId: UUID,
        name: String,
        defaultAccount: Boolean
    ): AccountImpl {
        if (defaultAccount) {
            AccountTable.update({ AccountTable.ownerId eq ownerId }) {
                it[AccountTable.defaultAccount] = false
            }
        }

        return AccountTable.insertReturning {
            it[this.ownerId] = ownerId
            it[this.accountId] = UUID.randomUUID()
            it[this.name] = name
            it[this.defaultAccount] = defaultAccount
        }.single().let(::fromResultRow)
    }

    override suspend fun findOrCreateDefaultAccount(
        ownerId: UUID
    ): AccountImpl = suspendTransaction {
        val existingDefaultAccount = AccountTable
            .leftJoin(AccountMemberTable, { AccountTable.id }, { AccountMemberTable.accountId })
            .selectAll()
            .where { (AccountTable.ownerId eq ownerId) and (AccountTable.defaultAccount eq true) }
            .toList()

        if (existingDefaultAccount.isNotEmpty()) {
            fromResultRowWithMembers(existingDefaultAccount)
        } else {
            createAccount0(ownerId, ownerId.toString(), true)
        }
    }

    override suspend fun addMemberToAccount(
        accountId: UUID,
        executor: UUID,
        target: UUID
    ): Pair<AccountMemberResult, UUID?> = suspendTransaction {
        val (accountID, ownerId) = selectAccountIDAndOwnerIdFromAccountId(accountId)
            ?: return@suspendTransaction AccountMemberResult.ACCOUNT_NOT_FOUND to null

        val inserted = AccountMemberTable.insertIgnore {
            it[this.accountId] = accountID
            it[this.memberId] = target
        }

        if (inserted.insertedCount == 0) {
            return@suspendTransaction AccountMemberResult.NOTHING_CHANGED to null
        }

        AccountMemberResult.SUCCESS to ownerId
    }

    override suspend fun removeMemberFromAccount(
        accountId: UUID,
        executor: UUID,
        target: UUID
    ): Pair<AccountMemberResult, UUID?> = suspendTransaction {
        val (accountID, ownerId) = selectAccountIDAndOwnerIdFromAccountId(accountId)
            ?: return@suspendTransaction AccountMemberResult.ACCOUNT_NOT_FOUND to null

        val deleted = AccountMemberTable.deleteWhere {
            (AccountMemberTable.accountId eq accountID) and
                    (AccountMemberTable.memberId eq target)
        }

        if (deleted == 0) {
            return@suspendTransaction AccountMemberResult.NOTHING_CHANGED to null
        }

        AccountMemberResult.SUCCESS to ownerId
    }

    private suspend fun selectAccountIDAndOwnerIdFromAccountId(accountId: UUID): Pair<ULong, UUID>? =
        AccountTable
            .select(AccountTable.id, AccountTable.ownerId)
            .where { AccountTable.accountId eq accountId }
            .limit(1)
            .singleOrNull()
            ?.let { row -> row[AccountTable.id].value to row[AccountTable.ownerId] }

    override suspend fun deleteAccount(accountId: UUID): Int = suspendTransaction {
        AccountTable.deleteWhere { AccountTable.accountId eq accountId }
    }

    override suspend fun completeAccountNameSuggestions(
        input: String,
        maxSuggestions: Int
    ): List<String> = suspendTransaction {
        AccountTable.select(AccountTable.name)
            .where { AccountTable.name like "$input%" }
            .orderBy(AccountTable.updatedAt)
            .limit(maxSuggestions)
            .map { it[AccountTable.name] }
            .toList()
    }

    private fun fromResultRow(row: ResultRow, members: Set<UUID> = emptySet()): AccountImpl =
        AccountImpl(
            ownerUuid = row[AccountTable.ownerId],
            accountId = row[AccountTable.accountId],
            name = row[AccountTable.name],
            defaultAccount = row[AccountTable.defaultAccount],
            members = members
        )

    private fun fromResultRowWithMembers(rows: List<ResultRow>): AccountImpl {
        val accountRow = rows.first()
        val memberUuids = rows.mapNotNull { row ->
            row.getOrNull(AccountMemberTable.memberId)
        }.toSet()

        return AccountImpl(
            ownerUuid = accountRow[AccountTable.ownerId],
            accountId = accountRow[AccountTable.accountId],
            name = accountRow[AccountTable.name],
            defaultAccount = accountRow[AccountTable.defaultAccount],
            members = memberUuids
        )
    }

    private fun fromMultiResultRowWithMembers(rows: List<ResultRow>): Set<AccountImpl> {
        if (rows.isEmpty()) return emptySet()

        return rows
            .groupBy { it[AccountTable.accountId] }
            .values
            .mapTo(mutableObjectSetOf(), ::fromResultRowWithMembers)
    }
}