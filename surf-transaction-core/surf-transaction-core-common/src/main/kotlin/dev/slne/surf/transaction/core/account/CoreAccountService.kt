package dev.slne.surf.transaction.core.account

import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.account.AccountService
import dev.slne.surf.transaction.api.account.member.results.AccountMemberResult
import dev.slne.surf.transaction.api.account.result.AccountCreationResult
import dev.slne.surf.transaction.api.account.result.AccountDeleteResult
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import java.util.UUID

@InternalTransactionApi
interface CoreAccountService : AccountService {
    suspend fun getAllAccountsByOwner(ownerUuid: UUID): Set<Account>
    suspend fun getDefaultAccount(playerUuid: UUID): Account
    suspend fun deleteAccount(account: Account): AccountDeleteResult
    suspend fun addMemberToAccount(accountId: UUID, executor: UUID, target: UUID): AccountMemberResult
    suspend fun removeMemberFromAccount(accountId: UUID, executor: UUID, target: UUID): AccountMemberResult
    suspend fun completeAccountNameSuggestions(input: String, maxSuggestions: Int): List<String>

    companion object {
        @OptIn(InternalTransactionApi::class)
        fun get() = AccountService.instance as CoreAccountService
    }
}
