package dev.slne.surf.transaction.api.account

import dev.slne.surf.transaction.api.account.member.AccountMemberOperations
import dev.slne.surf.transaction.api.account.result.AccountCreationResult
import dev.slne.surf.transaction.api.transactional.Transactional
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import net.kyori.adventure.text.Component
import java.util.*

@OptIn(InternalTransactionApi::class)
interface Account : Transactional, AccountMemberOperations {

    /**
     * Unique identifier for the account.
     */
    val accountId: UUID

    /**
     * The name of the account.
     */
    val name: String

    /**
     * The owner of the account.
     */
    val ownerUuid: UUID

    /**
     * Indicates whether this account is the default account for the owner.
     */
    val defaultAccount: Boolean

    /**
     * Converts the account information into a [Component] for display purposes.
     *
     * @return A [Component] representing the account information, suitable for use in user interfaces.
     */
    suspend fun asComponent(): Component

    companion object {
        /**
         * Minimum length for account names.
         */
        const val MIN_NAME_LENGTH = 3

        /**
         * Maximum length for account names.
         */
        const val MAX_NAME_LENGTH = 32

        suspend fun byId(accountId: UUID): Account? =
            AccountService.instance.getAccountByAccountId(accountId)

        suspend fun byName(name: String): Account? = AccountService.instance.getAccountByName(name)

        suspend fun create(
            owner: UUID,
            name: String
        ): AccountCreationResult = AccountService.instance.createAccount(owner, name)
    }

}