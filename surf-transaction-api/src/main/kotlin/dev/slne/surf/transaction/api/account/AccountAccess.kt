package dev.slne.surf.transaction.api.account

import dev.slne.surf.transaction.api.account.result.AccountCreationResult
import dev.slne.surf.transaction.api.account.result.AccountDeleteResult
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import java.util.*

@OptIn(InternalTransactionApi::class)
interface AccountAccess {

    val userUuid: UUID
    suspend fun getDefaultAccount(): Account
    suspend fun getAllAccounts(): Set<Account>
    suspend fun createAccount(name: String): AccountCreationResult
    suspend fun getAccountByName(accountName: String): Account?
    suspend fun deleteAccount(account: Account): AccountDeleteResult

}