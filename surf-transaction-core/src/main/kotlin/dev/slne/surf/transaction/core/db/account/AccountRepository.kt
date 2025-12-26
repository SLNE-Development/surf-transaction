package dev.slne.surf.transaction.core.db.account

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.dao.id.EntityID
import dev.slne.surf.transaction.api.account.member.results.AccountMemberResult
import dev.slne.surf.transaction.core.account.AccountImpl
import java.util.*

/**
 * Internal persistence abstraction for reading and mutating accounts and their members.
 *
 * Backed by [AccountRepositoryImpl] / Exposed tables.
 */
interface AccountRepository {

    /**
     * Resolves the internal database id for the given public [accountId].
     *
     * This method is not executed inside a database transaction.
     * The caller is responsible for ensuring transactional safety if required.
     *
     * @return the entity id or `null` if no account exists.
     */
    suspend fun findAccountIDByAccountId(accountId: UUID): Long?

    /**
     * Loads an account by its public [accountId] including member UUIDs.
     *
     * @return the account or `null` if not found.
     */
    suspend fun findAccountByAccountId(accountId: UUID): AccountImpl?

    /**
     * Loads an account by its unique [name] including member UUIDs.
     *
     * @return the account or `null` if not found.
     */
    suspend fun findAccountByName(name: String): AccountImpl?

    /**
     * Loads all accounts owned by [ownerUuid] including their members.
     */
    suspend fun findAccountsByOwner(ownerUuid: UUID): List<AccountImpl>

    /**
     * Checks whether an account with [name] exists.
     */
    suspend fun existsByAccountName(name: String): Boolean

    /**
     * Creates a new account for [ownerId].
     *
     * If [defaultAccount] is `true`, any existing default account for that owner will be unset.
     */
    suspend fun createAccount(ownerId: UUID, name: String, defaultAccount: Boolean): AccountImpl

    /**
     * Returns the current default account for [ownerId] or creates one if missing.
     *
     * The created default account uses the owner UUID string as name.
     */
    suspend fun findOrCreateDefaultAccount(ownerId: UUID): AccountImpl

    /**
     * Adds [target] as a member to the account identified by [accountId].
     *
     * @return a pair of the operation result and the account owner UUID (if available).
     */
    suspend fun addMemberToAccount(
        accountId: UUID,
        executor: UUID,
        target: UUID
    ): Pair<AccountMemberResult, UUID?>

    /**
     * Removes [target] from the members of the account identified by [accountId].
     *
     * @return a pair of the operation result and the account owner UUID (if available).
     */
    suspend fun removeMemberFromAccount(
        accountId: UUID,
        executor: UUID,
        target: UUID
    ): Pair<AccountMemberResult, UUID?>

    /**
     * Deletes the account identified by [accountId].
     *
     * @return number of deleted rows.
     */
    suspend fun deleteAccount(accountId: UUID): Int

    /**
     * Returns up to [maxSuggestions] account names containing [input], ordered by last update.
     */
    suspend fun completeAccountNameSuggestions(input: String, maxSuggestions: Int): List<String>

    companion object : AccountRepository by AccountRepositoryImpl()
}