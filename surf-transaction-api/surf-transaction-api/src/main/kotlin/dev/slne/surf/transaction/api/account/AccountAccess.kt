package dev.slne.surf.transaction.api.account

import dev.slne.surf.transaction.api.account.result.AccountCreationResult
import dev.slne.surf.transaction.api.account.result.AccountDeleteResult
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import org.jetbrains.annotations.ApiStatus
import java.util.*

/**
 * Provides user-scoped access to account management operations.
 *
 * An [AccountAccess] instance represents the view of accounts belonging to a
 * specific user and allows creating, retrieving, and deleting accounts owned
 * by that user.
 */
@OptIn(InternalTransactionApi::class)
@ApiStatus.NonExtendable
interface AccountAccess {

    /**
     * The UUID of the user this access instance belongs to.
     */
    val userUuid: UUID

    /**
     * Returns the default account of the user.
     *
     * @return the user's default account
     */
    suspend fun getDefaultAccount(): Account

    /**
     * Returns all accounts owned by the user.
     *
     * @return a set of all user accounts
     */
    suspend fun getAllAccounts(): Set<Account>

    /**
     * Creates a new account with the given [name] for the user.
     *
     * @param name the name of the account to create
     * @return the result of the account creation attempt
     */
    suspend fun createAccount(name: String): AccountCreationResult

    /**
     * Returns an account owned by the user with the given [accountName],
     * or `null` if no such account exists.
     *
     * @param accountName the name of the account
     */
    suspend fun getAccountByName(accountName: String): Account?

    /**
     * Deletes the given [account].
     *
     * @param account the account to delete
     * @return the result of the account deletion attempt
     */
    suspend fun deleteAccount(account: Account): AccountDeleteResult

}