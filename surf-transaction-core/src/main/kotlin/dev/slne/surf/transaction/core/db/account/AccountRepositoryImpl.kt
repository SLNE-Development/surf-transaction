package dev.slne.surf.transaction.core.db.account

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ResultRow
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.and
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.dao.id.EntityID
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.like
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.*
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.transaction.api.account.member.results.AccountMemberResult
import dev.slne.surf.transaction.core.account.AccountImpl
import dev.slne.surf.transaction.core.redis.RedisService
import kotlinx.coroutines.flow.*
import java.util.*
import kotlin.time.Duration.Companion.minutes

class AccountRepositoryImpl : AccountRepository {
    private val accountIdCache = RedisService.cache<UUID, ULong>("account_id", 10.minutes)

    override suspend fun findAccountIDByAccountId(accountId: UUID): ULong? {
        return accountIdCache.cachedOrLoadNullable(accountId) {
            AccountTable.select(AccountTable.id)
                .where { AccountTable.accountId eq accountId }
                .singleOrNull()
                ?.get(AccountTable.id)?.value
        }
    }

    override suspend fun findAccountByAccountId(
        accountId: UUID
    ): AccountImpl? = suspendTransaction {
        val accountResult = AccountTable.selectAll()
            .where { AccountTable.accountId eq accountId }
            .singleOrNull()

        if (accountResult == null) return@suspendTransaction null

        val accountID = accountResult[AccountTable.id]
        val members = findAccountMembers(accountID)

        fromResultRow(accountResult, members)
    }

    override suspend fun findAccountByName(name: String): AccountImpl? = suspendTransaction {
        val accountResult = AccountTable.selectAll()
            .where { AccountTable.name eq name }
            .singleOrNull()

        if (accountResult == null) return@suspendTransaction null

        val accountID = accountResult[AccountTable.id]
        val members = findAccountMembers(accountID)

        fromResultRow(accountResult, members)
    }

    override suspend fun findAccountsByOwner(
        ownerUuid: UUID
    ): List<AccountImpl> = suspendTransaction {
        AccountTable.selectAll()
            .where { AccountTable.ownerId eq ownerUuid }
            .map { row ->
                val accountID = row[AccountTable.id]
                val members = findAccountMembers(accountID)
                fromResultRow(row, members)
            }
            .toList()
    }

    private suspend fun findAccountMembers(accountID: EntityID<ULong>) =
        AccountMemberTable.selectAll()
            .where { AccountMemberTable.accountId eq accountID }
            .map(::memberFromResultRow)
            .toSet()

    override suspend fun existsByAccountName(name: String): Boolean = suspendTransaction {
        AccountTable.select(AccountTable.name)
            .where { AccountTable.name eq name }
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
        AccountTable.selectAll()
            .where { (AccountTable.ownerId eq ownerId) and (AccountTable.defaultAccount eq true) }
            .singleOrNull()
            ?.let { row ->
                val accountID = row[AccountTable.id]
                val members = findAccountMembers(accountID)
                return@suspendTransaction fromResultRow(row, members)
            }

        createAccount0(ownerId, ownerId.toString(), true)
    }

    override suspend fun addMemberToAccount(
        accountId: UUID,
        executor: UUID,
        target: UUID
    ): Pair<AccountMemberResult, UUID?> = suspendTransaction {
        val accountID = findAccountIDByAccountId(accountId)

        if (accountID == null) {
            return@suspendTransaction AccountMemberResult.ACCOUNT_NOT_FOUND to null
        }

        val alreadyMember = AccountMemberTable.select(AccountMemberTable.id)
            .where {
                (AccountMemberTable.accountId eq accountID) and
                        (AccountMemberTable.memberId eq target)
            }.count() > 0

        if (alreadyMember) {
            return@suspendTransaction AccountMemberResult.NOTHING_CHANGED to null
        }

        AccountMemberTable.insert {
            it[this.accountId] = accountID
            it[this.memberId] = target
        }

        val accountOwnerUuid = AccountTable.select(AccountTable.ownerId)
            .where { AccountTable.id eq accountID }
            .single()[AccountTable.ownerId]

        AccountMemberResult.SUCCESS to accountOwnerUuid
    }

    override suspend fun removeMemberFromAccount(
        accountId: UUID,
        executor: UUID,
        target: UUID
    ): Pair<AccountMemberResult, UUID?> = suspendTransaction {
        val accountID = findAccountIDByAccountId(accountId)

        if (accountID == null) {
            return@suspendTransaction AccountMemberResult.ACCOUNT_NOT_FOUND to null
        }

        val count = AccountMemberTable.deleteWhere {
            (AccountMemberTable.accountId eq accountID) and (AccountMemberTable.memberId eq target)
        }

        if (count == 0) {
            return@suspendTransaction AccountMemberResult.NOTHING_CHANGED to null
        }

        val accountOwnerUuid = AccountTable.select(AccountTable.ownerId)
            .where { AccountTable.id eq accountID }
            .single()[AccountTable.ownerId]

        AccountMemberResult.SUCCESS to accountOwnerUuid
    }

    override suspend fun deleteAccount(accountId: UUID): Int = suspendTransaction {
        AccountTable.deleteWhere { AccountTable.accountId eq accountId }
    }.also { accountIdCache.invalidate(accountId) }

    override suspend fun completeAccountNameSuggestions(
        input: String,
        maxSuggestions: Int
    ): List<String> = suspendTransaction {
        AccountTable.select(AccountTable.name)
            .where { AccountTable.name like "%$input%" }
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

    private fun memberFromResultRow(row: ResultRow) = row[AccountMemberTable.memberId]
}