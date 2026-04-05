package dev.slne.surf.transaction.api.account

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.transaction.api.account.result.AccountCreationResult
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import java.util.*

/**
 * Internal service for managing accounts.
 *
 * [AccountService] is responsible for resolving, creating, and managing
 * accounts within the transaction system. It is used internally by the
 * account and transaction APIs.
 *
 * This API is strictly internal to the Surf Transaction module and must not
 * be used by external consumers.
 */
@InternalTransactionApi
interface AccountService {

    /**
     * Returns an account by its unique [accountId], or `null` if none exists.
     *
     * @param accountId the account identifier
     * @return the resolved account or `null`
     */
    suspend fun getAccountByAccountId(accountId: UUID): Account?

    /**
     * Returns an account by its [accountName], or `null` if none exists.
     *
     * @param accountName the name of the account
     * @return the resolved account or `null`
     */
    suspend fun getAccountByName(accountName: String): Account?

    /**
     * Creates a new account for the given owner.
     *
     * @param ownerUuid the UUID of the account owner
     * @param name the name of the new account
     *
     * @return the result of the account creation attempt
     */
    suspend fun createAccount(ownerUuid: UUID, name: String): AccountCreationResult

    companion object {
        val instance = requiredService<AccountService>()
        fun init() = Unit
    }
}