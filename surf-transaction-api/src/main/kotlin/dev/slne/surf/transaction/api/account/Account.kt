package dev.slne.surf.transaction.api.account

import dev.slne.surf.transaction.api.account.Account.Companion.MAX_NAME_LENGTH
import dev.slne.surf.transaction.api.account.Account.Companion.MIN_NAME_LENGTH
import dev.slne.surf.transaction.api.account.member.AccountMemberOperations
import dev.slne.surf.transaction.api.account.result.AccountCreationResult
import dev.slne.surf.transaction.api.transactional.Transactional
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import net.kyori.adventure.text.Component
import org.jetbrains.annotations.ApiStatus
import java.util.*

/**
 * Represents an account within the transaction system.
 *
 * An [Account] holds balances for one or more currencies and supports
 * transactional operations such as deposits, withdrawals, and transfers.
 * It also provides member management via [AccountMemberOperations].
 *
 * Accounts are managed by the [AccountService] and should be accessed
 * through the provided lookup and creation methods.
 */
@OptIn(InternalTransactionApi::class)
@ApiStatus.NonExtendable
interface Account : Transactional, AccountMemberOperations {

    /**
     * The unique identifier of this account.
     */
    val accountId: UUID

    /**
     * The human-readable name of this account.
     *
     * The name length must be within [MIN_NAME_LENGTH] and [MAX_NAME_LENGTH].
     */
    val name: String

    /**
     * The UUID of the account owner.
     *
     * The owner typically has full permissions on the account.
     */
    val ownerUuid: UUID

    /**
     * Whether this account is the default account of its owner.
     */
    val defaultAccount: Boolean

    /**
     * Returns a component representation of this account.
     *
     * This component is typically used for user-facing displays and may
     * include formatting or additional contextual information.
     *
     * @return a display component representing this account
     */
    suspend fun asComponent(): Component

    companion object {
        /**
         * The minimum allowed length of an account name.
         */
        const val MIN_NAME_LENGTH = 3

        /**
         * The maximum allowed length of an account name.
         */
        const val MAX_NAME_LENGTH = 32


        /**
         * Returns an account by its unique [accountId], or `null` if none exists.
         *
         * @param accountId the account identifier
         */
        suspend fun byId(accountId: UUID): Account? =
            AccountService.getAccountByAccountId(accountId)

        /**
         * Returns an account by its [name], or `null` if none exists.
         *
         * @param name the name of the account
         */
        suspend fun byName(name: String): Account? = AccountService.getAccountByName(name)

        /**
         * Creates a new account for the given [owner] with the specified [name].
         *
         * @param owner the UUID of the account owner
         * @param name the name of the new account
         *
         * @return the result of the account creation attempt
         */
        suspend fun create(
            owner: UUID,
            name: String
        ): AccountCreationResult = AccountService.createAccount(owner, name)
    }

}