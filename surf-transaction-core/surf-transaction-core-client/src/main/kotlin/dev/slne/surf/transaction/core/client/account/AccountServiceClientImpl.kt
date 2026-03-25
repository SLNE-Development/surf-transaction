package dev.slne.surf.transaction.core.client.account

import com.google.auto.service.AutoService
import dev.slne.surf.microservice.api.rabbit.client.ClientRabbitMQApi
import dev.slne.surf.surfapi.core.api.util.requiredService
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.account.AccountService
import dev.slne.surf.transaction.api.account.member.results.AccountMemberResult
import dev.slne.surf.transaction.api.account.result.AccountCreationResult
import dev.slne.surf.transaction.api.account.result.AccountDeleteResult
import dev.slne.surf.transaction.core.account.CoreAccountService
import dev.slne.surf.transaction.core.rabbit.account.AddMemberToAccountRequest
import dev.slne.surf.transaction.core.rabbit.account.CompleteAccountNameSuggestionsRequest
import dev.slne.surf.transaction.core.rabbit.account.CreateAccountRequest
import dev.slne.surf.transaction.core.rabbit.account.DeleteAccountRequest
import dev.slne.surf.transaction.core.rabbit.account.GetAccountByAccountIdRequest
import dev.slne.surf.transaction.core.rabbit.account.GetAccountByNameRequest
import dev.slne.surf.transaction.core.rabbit.account.GetAllAccountsByOwnerRequest
import dev.slne.surf.transaction.core.rabbit.account.GetDefaultAccountRequest
import dev.slne.surf.transaction.core.rabbit.account.RemoveMemberFromAccountRequest
import java.util.UUID

@AutoService(AccountService::class)
class AccountServiceClientImpl : CoreAccountService {
    private val rabbitApi get() = requiredService<ClientRabbitMQApi>()

    override suspend fun getAccountByAccountId(accountId: UUID): Account? {
        val response = rabbitApi.sendRequest(GetAccountByAccountIdRequest(accountId))
        return response.account
    }

    override suspend fun getAccountByName(accountName: String): Account? {
        val response = rabbitApi.sendRequest(GetAccountByNameRequest(accountName.lowercase()))
        return response.account
    }

    override suspend fun createAccount(ownerUuid: UUID, name: String): AccountCreationResult {
        val response = rabbitApi.sendRequest(CreateAccountRequest(ownerUuid, name))
        return if (response.account != null) {
            AccountCreationResult.Success(response.account)
        } else {
            AccountCreationResult.Failed(
                response.failureReason ?: AccountCreationResult.FailureReason.NAME_ALREADY_EXISTS
            )
        }
    }

    override suspend fun getAllAccountsByOwner(ownerUuid: UUID): Set<Account> {
        val response = rabbitApi.sendRequest(GetAllAccountsByOwnerRequest(ownerUuid))
        return response.accounts.toSet()
    }

    override suspend fun getDefaultAccount(playerUuid: UUID): Account {
        val response = rabbitApi.sendRequest(GetDefaultAccountRequest(playerUuid))
        return response.account
    }

    override suspend fun deleteAccount(account: Account): AccountDeleteResult {
        val response = rabbitApi.sendRequest(DeleteAccountRequest(account.accountId))
        return response.result
    }

    override suspend fun addMemberToAccount(
        accountId: UUID,
        executor: UUID,
        target: UUID
    ): AccountMemberResult {
        val response = rabbitApi.sendRequest(AddMemberToAccountRequest(accountId, executor, target))
        return response.result
    }

    override suspend fun removeMemberFromAccount(
        accountId: UUID,
        executor: UUID,
        target: UUID
    ): AccountMemberResult {
        val response =
            rabbitApi.sendRequest(RemoveMemberFromAccountRequest(accountId, executor, target))
        return response.result
    }

    override suspend fun completeAccountNameSuggestions(
        input: String,
        maxSuggestions: Int
    ): List<String> {
        val response =
            rabbitApi.sendRequest(CompleteAccountNameSuggestionsRequest(input, maxSuggestions))
        return response.suggestions
    }
}
