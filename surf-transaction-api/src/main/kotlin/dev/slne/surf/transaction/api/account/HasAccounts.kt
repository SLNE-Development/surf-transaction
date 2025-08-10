package dev.slne.surf.transaction.api.account

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.transaction.api.util.InternalTransactionApi

@OptIn(InternalTransactionApi::class)
interface HasAccounts {

    /**
     * Represents the cloud player associated with this account holder.
     */
    val cloudPlayer: OfflineCloudPlayer

    /**
     * Retrieves the default account for this player.
     *
     * @return the default [Account] associated with this player
     */
    suspend fun getDefaultAccount() =
        InternalAccountBridge.instance.getDefaultAccount(cloudPlayer)

    /**
     * Retrieves all accounts associated with this player.
     *
     * @return a set of [Account]s owned by this player
     */
    suspend fun getAllAccounts() =
        InternalAccountBridge.instance.getAccounts(cloudPlayer)

    /**
     * Creates a new account for this player with the specified [name].
     *
     * @param name the name of the new account; must be non-empty
     * @return the newly created [Account]
     */
    suspend fun createAccount(name: String) =
        InternalAccountBridge.instance.createAccount(cloudPlayer, name)

    /**
     * Retrieves the account with the specified [accountName] for this player.
     *
     * @param accountName the name of the account to retrieve; must be non-empty
     * @return the [Account] matching [accountName], or `null` if not found
     */
    suspend fun getAccountByName(accountName: String) =
        InternalAccountBridge.instance.getAccountByName(accountName)

    /**
     * Deletes the specified [account] from this player's accounts.
     *
     * @param account the account to delete; must be non-null
     * @return an [AccountDeleteResult] indicating success or failure
     */
    suspend fun deleteAccount(account: Account) =
        InternalAccountBridge.instance.deleteAccount(account)

}