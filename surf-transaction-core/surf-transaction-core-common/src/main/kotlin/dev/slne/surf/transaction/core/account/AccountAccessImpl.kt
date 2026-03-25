package dev.slne.surf.transaction.core.account

import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.account.AccountAccess
import dev.slne.surf.transaction.api.account.result.AccountCreationResult
import dev.slne.surf.transaction.api.account.result.AccountDeleteResult
import java.util.UUID

class AccountAccessImpl(override val userUuid: UUID) : AccountAccess {
    override suspend fun getDefaultAccount(): Account {
        return CoreAccountService.get().getDefaultAccount(userUuid)
    }

    override suspend fun getAllAccounts(): Set<Account> {
        return CoreAccountService.get().getAllAccountsByOwner(userUuid)
    }

    override suspend fun createAccount(name: String): AccountCreationResult {
        return CoreAccountService.get().createAccount(userUuid, name)
    }

    override suspend fun getAccountByName(accountName: String): Account? {
        return CoreAccountService.get().getAccountByName(accountName)
    }

    override suspend fun deleteAccount(account: Account): AccountDeleteResult {
        return CoreAccountService.get().deleteAccount(account)
    }
}
