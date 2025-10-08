package dev.slne.surf.transaction.api.account

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.transaction.api.InternalTransactionApiBridge
import dev.slne.surf.transaction.api.util.ComponentResult
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.springframework.beans.factory.getBean
import java.util.*

@InternalTransactionApi
interface InternalAccountBridge {
    /**
     * Retrieves an account by its unique identifier.
     *
     * @param accountId The unique identifier of the account to retrieve.
     * @return The account associated with the given identifier, or null if no such account exists.
     */
    suspend fun getAccountByAccountId(accountId: UUID): Account?

    /**
     * Creates a new account for a specified player.
     *
     * @param owner The player who will own the new account.
     * @param name The name of the new account.
     * @return A result indicating the success or failure of the account creation.
     */
    suspend fun createAccount(owner: OfflineCloudPlayer, name: String): ComponentResult

    /**
     * Retrieves all accounts owned by a specific player.
     *
     * @param owner The player whose accounts are to be retrieved.
     * @return A set of accounts owned by the specified player.
     */
    suspend fun getAllAccountsByOwner(owner: OfflineCloudPlayer): ObjectSet<out Account>

    /**
     * Retrieves an account by its name.
     *
     * @param name The name of the account to retrieve.
     * @return The account associated with the given name, or null if no such account exists.
     */
    suspend fun getAccountByName(name: String): Account?

    /**
     * Retrieves all accounts associated with a specific player.
     *
     * @param player The player whose accounts are to be retrieved.
     * @return A set of accounts associated with the specified player.
     */
    suspend fun getAccounts(player: OfflineCloudPlayer): ObjectSet<out Account>

    /**
     * Retrieves the default account for a specified player.
     *
     * @param player The player whose default account is to be retrieved.
     * @return The default account associated with the specified player.
     * @throws IllegalStateException if no default account is found for the player.
     */
    suspend fun getDefaultAccount(player: OfflineCloudPlayer): Account =
        getDefaultAccountOrNull(player)
            ?: error("Default account not found for player: ${player.uuid}")

    /**
     * Retrieves the default account for a specified player, or returns null if no default account exists
     *
     * @param player The player whose default account is to be retrieved.
     * @return The default account associated with the specified player, or null if no default account exists.
     */
    suspend fun getDefaultAccountOrNull(player: OfflineCloudPlayer): Account?

    /**
     * Deletes a specified account.
     *
     * @param account The account to be deleted.
     * @return A result indicating the success or failure of the account deletion.
     */
    suspend fun deleteAccount(account: Account): ComponentResult

    /**
     * Adds a member to the specified account.
     *
     * @param account The account to which the member will be added.
     * @param executor The player executing the addition of the member.
     * @param target The player to be added as a member.
     * @return A result indicating the success or failure of the member addition.
     */
    suspend fun addMemberToAccount(
        account: Account,
        executor: OfflineCloudPlayer,
        target: OfflineCloudPlayer
    ): ComponentResult

    /**
     * Removes a member from the specified account.
     *
     * @param account The account from which the member will be removed.
     * @param executor The player executing the removal of the member.
     * @param target The player to be removed as a member.
     * @return A result indicating the success or failure of the member removal.
     */
    suspend fun removeMemberFromAccount(
        account: Account,
        executor: OfflineCloudPlayer,
        target: OfflineCloudPlayer
    ): ComponentResult

    val descriptor: SerialDescriptor
    fun serialize(encoder: Encoder, value: Account)
    fun deserialize(decoder: Decoder): Account

    companion object {
        val instance get() = InternalTransactionApiBridge.instance.context.getBean<InternalAccountBridge>()
    }
}