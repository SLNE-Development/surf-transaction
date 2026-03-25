package dev.slne.surf.transaction.core.client.account

import com.google.auto.service.AutoService
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.account.AccountService
import dev.slne.surf.transaction.api.account.member.results.AccountMemberResult
import dev.slne.surf.transaction.api.account.result.AccountCreationResult
import dev.slne.surf.transaction.api.account.result.AccountCreationResult.FailureReason
import dev.slne.surf.transaction.api.account.result.AccountDeleteResult
import dev.slne.surf.transaction.core.client.rabbitApi
import dev.slne.surf.transaction.core.client.redis.RedisService
import dev.slne.surf.transaction.core.common.account.AccountImpl
import dev.slne.surf.transaction.core.common.account.CoreAccountService
import dev.slne.surf.transaction.core.common.protocol.account.addMember.AddMemberToAccountRequestPacket
import dev.slne.surf.transaction.core.common.protocol.account.create.CreateAccountRequestPacket
import dev.slne.surf.transaction.core.common.protocol.account.delete.DeleteAccountRequestPacket
import dev.slne.surf.transaction.core.common.protocol.account.existsByAccountName.ExistsAccountByAccountNameRequestPacket
import dev.slne.surf.transaction.core.common.protocol.account.findAllByOwner.FindAllAccountsByOwnerRequestPacket
import dev.slne.surf.transaction.core.common.protocol.account.findByAccountId.FindAccountByAccountIdRequestPacket
import dev.slne.surf.transaction.core.common.protocol.account.findByAccountName.FindAccountByAccountNameRequestPacket
import dev.slne.surf.transaction.core.common.protocol.account.findOrCreateDefaultAccountByPlayerUuid.FindOrCreateDefaultAccountByPlayerUuidRequestPacket
import dev.slne.surf.transaction.core.common.protocol.account.removeMember.RemoveMemberFromAccountRequestPacket
import dev.slne.surf.transaction.core.common.protocol.account.suggestion.CompleteAccountNameSuggestionsRequestPacket
import java.util.*
import kotlin.time.Duration.Companion.minutes

@AutoService(AccountService::class)
class AccountServiceImpl : CoreAccountService {
    private val defaultAccountCache = RedisService.cache<UUID, AccountImpl>("default_account", 10.minutes)

    override suspend fun getAccountByAccountId(accountId: UUID): Account? {
        val request = FindAccountByAccountIdRequestPacket(accountId)
        return rabbitApi.sendRequest(request).account
    }

    override suspend fun getAccountByName(accountName: String): Account? {
        val request = FindAccountByAccountNameRequestPacket(accountName.lowercase())
        return rabbitApi.sendRequest(request).account
    }

    override suspend fun createAccount(
        ownerUuid: UUID,
        name: String
    ): AccountCreationResult {
        val name = name.trim().replace(" ", "_").lowercase()

        if (name.length < Account.MIN_NAME_LENGTH) {
            return AccountCreationResult.Failed(FailureReason.NAME_TOO_SHORT)
        }

        if (name.length > Account.MAX_NAME_LENGTH) {
            return AccountCreationResult.Failed(FailureReason.NAME_TOO_LONG)
        }

        if (runCatching { UUID.fromString(name) }.isFailure) {
            return AccountCreationResult.Failed(FailureReason.NAME_IS_UUID)
        }

        val alreadyExistsRequest = ExistsAccountByAccountNameRequestPacket(name)
        val alreadyExists = rabbitApi.sendRequest(alreadyExistsRequest).value
        if (alreadyExists) {
            return AccountCreationResult.Failed(FailureReason.NAME_ALREADY_EXISTS)
        }

        val createRequest = CreateAccountRequestPacket(ownerUuid, name, false)
        val (created) = rabbitApi.sendRequest(createRequest)

        return AccountCreationResult.Success(created)
    }

    override suspend fun getAllAccountsByOwner(ownerUuid: UUID): Set<Account> {
        val request = FindAllAccountsByOwnerRequestPacket(ownerUuid)
        return rabbitApi.sendRequest(request).accounts
    }

    override suspend fun getDefaultAccount(playerUuid: UUID): Account {
        return defaultAccountCache.cachedOrLoad(playerUuid) {
            val request = FindOrCreateDefaultAccountByPlayerUuidRequestPacket(playerUuid)
            rabbitApi.sendRequest(request).account
        }
    }

    override suspend fun deleteAccount(account: Account): AccountDeleteResult {
        if (account.defaultAccount) {
            return AccountDeleteResult.DEFAULT_ACCOUNT_CANNOT_BE_DELETED
        }

        val request = DeleteAccountRequestPacket(account.accountId)
        val count = rabbitApi.sendRequest(request).value
        if (count == 0L) {
            return AccountDeleteResult.ACCOUNT_NOT_FOUND
        }

        return AccountDeleteResult.SUCCESS
    }

    override suspend fun addMemberToAccount(
        accountId: UUID,
        executor: UUID,
        target: UUID
    ): AccountMemberResult {
        val request = AddMemberToAccountRequestPacket(accountId, executor, target)
        val (result, accountOwnerUuid) = rabbitApi.sendRequest(request)

        if (result == AccountMemberResult.SUCCESS && accountOwnerUuid != null) {
            defaultAccountCache.invalidate(accountOwnerUuid)
        }

        return result
    }

    override suspend fun removeMemberFromAccount(
        accountId: UUID,
        executor: UUID,
        target: UUID
    ): AccountMemberResult {
        val request = RemoveMemberFromAccountRequestPacket(accountId, executor, target)
        val (result, accountOwnerUuid) = rabbitApi.sendRequest(request)

        if (result == AccountMemberResult.SUCCESS && accountOwnerUuid != null) {
            defaultAccountCache.invalidate(accountOwnerUuid)
        }

        return result
    }

    override suspend fun completeAccountNameSuggestions(input: String, maxSuggestions: Int): List<String> {
        val inputLowerCase = input.lowercase()
        val request = CompleteAccountNameSuggestionsRequestPacket(inputLowerCase, maxSuggestions)
        return rabbitApi.sendRequest(request).completions
    }

    companion object {
        fun get() = AccountService.instance as AccountServiceImpl
    }
}