package dev.slne.surf.transaction.core.common.account

import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.account.AccountAccess
import dev.slne.surf.transaction.api.account.result.AccountCreationResult
import dev.slne.surf.transaction.api.account.result.AccountDeleteResult
import java.util.*

class AccountAccessImpl(override val userUuid: UUID) : AccountAccess {
    override suspend fun getDefaultAccount(): Account {
        return CoreAccountService.getDefaultAccount(userUuid)
    }

    override suspend fun getAllAccounts(): Set<Account> {
        return CoreAccountService.getAllAccountsByOwner(userUuid)
    }

    override suspend fun createAccount(name: String): AccountCreationResult {
        return CoreAccountService.createAccount(userUuid, name)
    }

    override suspend fun getAccountByName(accountName: String): Account? {
        return CoreAccountService.getAccountByName(accountName)
    }

    override suspend fun deleteAccount(account: Account): AccountDeleteResult {
        return CoreAccountService.deleteAccount(account)
    }
}