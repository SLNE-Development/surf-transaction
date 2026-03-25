package dev.slne.surf.transaction.core.client.account

import com.google.auto.service.AutoService
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.account.AccountService
import dev.slne.surf.transaction.api.account.member.results.AccountMemberResult
import dev.slne.surf.transaction.api.account.result.AccountCreationResult
import dev.slne.surf.transaction.api.account.result.AccountDeleteResult
import dev.slne.surf.transaction.core.common.account.AccountImpl
import dev.slne.surf.transaction.core.common.account.CoreAccountService
import dev.slne.surf.transaction.core.client.redis.RedisService
import java.util.*
import kotlin.time.Duration.Companion.minutes

@AutoService(AccountService::class)
class AccountServiceImpl : CoreAccountService {
    private val defaultAccountCache = RedisService.cache<UUID, AccountImpl>("default_account", 10.minutes)

    override suspend fun getAccountByAccountId(accountId: UUID): Account? {
        return AccountRepository.Companion.findAccountByAccountId(accountId)
    }

    override suspend fun getAccountByName(accountName: String): Account? {
        return AccountRepository.Companion.findAccountByName(accountName.lowercase())
    }

    override suspend fun createAccount(
        ownerUuid: UUID,
        name: String
    ): AccountCreationResult {
        val name = name.trim().replace(" ", "_").lowercase()

        if (name.length < Account.Companion.MIN_NAME_LENGTH) {
            return AccountCreationResult.Failed(
                AccountCreationResult.FailureReason.NAME_TOO_SHORT
            )
        }

        if (name.length > Account.Companion.MAX_NAME_LENGTH) {
            return AccountCreationResult.Failed(
                AccountCreationResult.FailureReason.NAME_TOO_LONG
            )
        }

        if (runCatching { UUID.fromString(name) }.getOrNull() != null) {
            return AccountCreationResult.Failed(
                AccountCreationResult.FailureReason.NAME_IS_UUID
            )
        }

        val alreadyExists = AccountRepository.Companion.existsByAccountName(name)
        if (alreadyExists) {
            return AccountCreationResult.Failed(
                AccountCreationResult.FailureReason.NAME_ALREADY_EXISTS
            )
        }

        return AccountCreationResult.Success(
            AccountRepository.Companion.createAccount(
                ownerUuid,
                name,
                false
            )
        )
    }

    override suspend fun getAllAccountsByOwner(ownerUuid: UUID): Set<Account> {
        return AccountRepository.Companion.findAccountsByOwner(ownerUuid).toSet()
    }

    override suspend fun getDefaultAccount(playerUuid: UUID): Account {
        return defaultAccountCache.cachedOrLoad(playerUuid) {
            AccountRepository.Companion.findOrCreateDefaultAccount(playerUuid)
        }
    }

    override suspend fun deleteAccount(account: Account): AccountDeleteResult {
        if (account.defaultAccount) {
            return AccountDeleteResult.DEFAULT_ACCOUNT_CANNOT_BE_DELETED
        }

        val count = AccountRepository.Companion.deleteAccount(account.accountId)
        if (count == 0) {
            return AccountDeleteResult.ACCOUNT_NOT_FOUND
        }

        return AccountDeleteResult.SUCCESS
    }

    override suspend fun addMemberToAccount(
        accountId: UUID,
        executor: UUID,
        target: UUID
    ): AccountMemberResult {
        val (result, accountOwnerUuid) = AccountRepository.Companion.addMemberToAccount(
            accountId,
            executor,
            target
        )

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
        val (result, accountOwnerUuid) = AccountRepository.Companion.removeMemberFromAccount(
            accountId,
            executor,
            target
        )

        if (result == AccountMemberResult.SUCCESS && accountOwnerUuid != null) {
            defaultAccountCache.invalidate(accountOwnerUuid)
        }

        return result
    }

    override suspend fun completeAccountNameSuggestions(input: String, maxSuggestions: Int): List<String> {
        val inputLowerCase = input.lowercase()
        return AccountRepository.Companion.completeAccountNameSuggestions(inputLowerCase, maxSuggestions)
    }

    companion object {
        fun get() = AccountService.Companion.instance as AccountServiceImpl
    }
}