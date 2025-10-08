package dev.slne.surf.transaction.api.account

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.transaction.api.account.member.HasMembers
import dev.slne.surf.transaction.api.account.result.AccountCreationResult
import dev.slne.surf.transaction.api.user.HasTransactions
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import java.util.*

@OptIn(InternalTransactionApi::class)
@Serializable(with = AccountSerializer::class)
interface Account : HasTransactions, HasMembers {

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
    val owner: OfflineCloudPlayer

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
        val MIN_NAME_LENGTH = 3

        /**
         * Maximum length for account names.
         */
        val MAX_NAME_LENGTH = 32

        /**
         * Retrieves an account by its unique identifier.
         *
         * @param accountId The unique identifier of the account to retrieve.
         * @return The [Account] associated with the given [accountId], or null if no such account exists.
         */
        suspend operator fun get(accountId: UUID?): Account? =
            accountId?.let { InternalAccountBridge.instance.getAccountByAccountId(it) }

        /**
         * Creates a new account with the specified owner and name.
         *
         * @param owner The owner of the account.
         * @param name The name of the account.
         * @return An [dev.slne.surf.transaction.api.account.result.AccountCreationResult] indicating the success or failure of the account creation.
         */
        suspend fun create(
            owner: OfflineCloudPlayer,
            name: String
        ): AccountCreationResult = InternalAccountBridge.instance.createAccount(owner, name)
    }

}