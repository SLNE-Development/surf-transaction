package dev.slne.surf.transaction.core.db.account

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.dao.id.EntityID
import dev.slne.surf.transaction.api.account.member.results.AccountMemberResult
import dev.slne.surf.transaction.api.account.result.AccountDeleteResult
import dev.slne.surf.transaction.core.account.AccountImpl
import java.util.*

interface AccountRepository {

    suspend fun findAccountIDByAccountId(accountId: UUID): EntityID<Long>?
    suspend fun findAccountByAccountId(accountId: UUID): AccountImpl?
    suspend fun findAccountByName(name: String): AccountImpl?
    suspend fun findAccountsByOwner(ownerUuid: UUID): List<AccountImpl>

    suspend fun existsByAccountName(name: String): Boolean
    suspend fun createAccount(ownerId: UUID, name: String, defaultAccount: Boolean): AccountImpl

    suspend fun findOrCreateDefaultAccount(ownerId: UUID): AccountImpl

    suspend fun addMemberToAccount(
        accountId: UUID,
        executor: UUID,
        target: UUID
    ): AccountMemberResult

    suspend fun removeMemberFromAccount(
        accountId: UUID,
        executor: UUID,
        target: UUID
    ): AccountMemberResult

    suspend fun deleteAccount(accountId: UUID): Int

    suspend fun completeAccountNameSuggestions(input: String, maxSuggestions: Int): List<String>

    companion object : AccountRepository by AccountRepositoryImpl()
}