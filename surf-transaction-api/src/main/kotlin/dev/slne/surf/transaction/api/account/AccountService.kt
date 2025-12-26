package dev.slne.surf.transaction.api.account

import dev.slne.surf.surfapi.core.api.util.requiredService
import dev.slne.surf.transaction.api.account.result.AccountCreationResult
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import java.util.*

@InternalTransactionApi
interface AccountService {
    suspend fun getAccountByAccountId(accountId: UUID): Account?
    suspend fun getAccountByName(accountName: String): Account?
    suspend fun createAccount(ownerUuid: UUID, name: String): AccountCreationResult

    companion object {
        val instance = requiredService<AccountService>()
    }
}