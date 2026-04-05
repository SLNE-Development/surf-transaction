package dev.slne.surf.transaction.core.account

import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.account.AccountAccess
import dev.slne.surf.transaction.api.account.result.AccountCreationResult
import dev.slne.surf.transaction.api.account.result.AccountDeleteResult
import java.util.UUID

class AccountAccessImpl(override val userUuid: UUID) : AccountAccess {
    override suspend fun getDefaultAccount(): Account {
        return AccountServiceImpl.get().getDefaultAccount(userUuid)
    }

    override suspend fun getAllAccounts(): Set<Account> {
        return AccountServiceImpl.get().getAllAccountsByOwner(userUuid)
    }

    override suspend fun createAccount(name: String): AccountCreationResult {
        return AccountServiceImpl.get().createAccount(userUuid, name)
    }

    override suspend fun getAccountByName(accountName: String): Account? {
        return AccountServiceImpl.get().getAccountByName(accountName)
    }

    override suspend fun deleteAccount(account: Account): AccountDeleteResult {
        return AccountServiceImpl.get().deleteAccount(account)
    }
}