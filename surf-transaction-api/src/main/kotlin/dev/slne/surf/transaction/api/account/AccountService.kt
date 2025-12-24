package dev.slne.surf.transaction.api.account

import dev.slne.surf.surfapi.core.api.util.requiredService
import dev.slne.surf.transaction.api.account.result.AccountCreationResult
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import java.util.*

@InternalTransactionApi
interface AccountService {
    /**
     * Retrieves an account by its unique identifier.
     *
     * @param accountId The unique identifier of the account to retrieve.
     * @return The account associated with the given identifier, or null if no such account exists.
     */
    suspend fun getAccountByAccountId(accountId: UUID): Account?

    suspend fun getAccountByName(accountName: String): Account?

    /**
     * Creates a new account for a specified player.
     *
     * @param owner The player who will own the new account.
     * @param name The name of the new account.
     * @return A result indicating the success or failure of the account creation.
     */
    suspend fun createAccount(ownerUuid: UUID, name: String): AccountCreationResult

    companion object {
        val instance = requiredService<AccountService>()
    }
}