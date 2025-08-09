package dev.slne.surf.transaction.server.account

import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.util.mutableObjectSetOf
import dev.slne.surf.cloud.api.common.util.toObjectSet
import dev.slne.surf.cloud.api.server.plugin.CoroutineTransactional
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.account.AccountDeleteResult
import dev.slne.surf.transaction.core.account.AccountImpl
import dev.slne.surf.transaction.server.account.db.AccountEntity
import dev.slne.surf.transaction.server.account.db.AccountTable
import it.unimi.dsi.fastutil.objects.ObjectSet
import org.jetbrains.exposed.sql.and
import org.springframework.stereotype.Repository
import java.util.*

@CoroutineTransactional
@Repository
class AccountRepository {

    suspend fun deleteAccount(accountId: UUID): AccountDeleteResult {
        val accountEntity = fetchByAccountId(accountId)
            ?: return AccountDeleteResult.Failure(AccountDeleteResult.FailureReason.ACCOUNT_NOT_FOUND)

        accountEntity.delete()

        return AccountDeleteResult.Success(accountEntity.toApi())
    }

    /**
     * Create a new account for the given [OfflineCloudPlayer] with the specified name.
     * Also marks the current default account as non-default if it exists.
     *
     * @param owner The [OfflineCloudPlayer] who will own the account.
     * @param name The name of the account to be created.
     * @param defaultAccount Whether this account should be marked as the default account for the owner.
     *
     * @return The newly created [Account].
     */
    suspend fun createAccount(
        owner: OfflineCloudPlayer,
        name: String,
        defaultAccount: Boolean
    ): AccountImpl {
        val currentDefaultAccount = fetchDefaultAccount(owner)

        if (currentDefaultAccount != null) {
            currentDefaultAccount.defaultAccount = false
        }

        return AccountEntity.new {
            this.owner = owner.uuid
            this.accountId = UUID.randomUUID()
            this.name = name
            this.defaultAccount = defaultAccount
        }.toApi()
    }

    /**
     * Get the default account for a player
     *
     * @param player The [OfflineCloudPlayer] to get the default account for
     *
     * @return The default [Account] for the player
     */
    suspend fun getDefaultAccount(
        player: OfflineCloudPlayer
    ) = fetchDefaultAccount(player)?.toApi() ?: run {
        val cloudPlayer = player.player ?: return@run null

        createByPlayer(cloudPlayer).toApi()
    }

    /**
     * Fetches the default account for the given [OfflineCloudPlayer].
     *
     * @param player The [OfflineCloudPlayer] whose default account is to be fetched.
     * @return The [AccountEntity] representing the default account, or null if no default account exists.
     */
    suspend fun fetchDefaultAccount(
        player: OfflineCloudPlayer
    ): AccountEntity? = AccountEntity.find {
        (AccountTable.owner eq player.uuid) and (AccountTable.defaultAccount eq true)
    }.singleOrNull()

    /**
     * Fetches all accounts owned by the given [OfflineCloudPlayer].
     *
     * @param player The [OfflineCloudPlayer] whose accounts are to be fetched.
     * @return A set of [AccountEntity] objects representing the accounts owned by the player.
     */
    suspend fun fetchByPlayer(player: OfflineCloudPlayer): ObjectSet<AccountEntity> =
        AccountEntity.find { AccountTable.owner eq player.uuid }.toObjectSet()

    /**
     * Fetches all accounts owned by the given [OfflineCloudPlayer] and converts them to API representation.
     *
     * @param owner The [OfflineCloudPlayer] whose accounts are to be fetched.
     * @return A set of [Account] objects representing the accounts owned by the player.
     */
    suspend fun getAllAccountsByOwner(owner: OfflineCloudPlayer) =
        fetchByPlayer(owner).mapTo(mutableObjectSetOf()) { it.toApi() }

    /**
     * Creates a new account for the given [CloudPlayer].
     *
     * @param player The [CloudPlayer] for whom the account is being created.
     * @return The newly created [AccountEntity].
     */
    suspend fun createByPlayer(player: CloudPlayer) = AccountEntity.new {
        owner = player.uuid
        accountId = UUID.randomUUID()
        name = player.name
    }

    /**
     * Fetches an [AccountEntity] by its account ID.
     *
     * @param accountId The UUID of the account to fetch.
     * @return The [AccountEntity] if found, or null if not found.
     */
    suspend fun fetchByAccountId(accountId: UUID): AccountEntity? =
        AccountEntity.find { AccountTable.accountId eq accountId }.singleOrNull()

    /**
     * Fetches an [AccountEntity] by its name.
     *
     * @param name The name of the account to fetch.
     * @return The [AccountEntity] if found, or null if not found.
     */
    suspend fun fetchByAccountName(name: String): AccountEntity? =
        AccountEntity.find { AccountTable.name like name }.singleOrNull()

    /**
     * Fetches an [Account] by its account ID and converts it to the API representation.
     *
     * @param accountId The UUID of the account to fetch.
     * @return The [Account] if found, or null if not found.
     */
    suspend fun getByAccountId(accountId: UUID) =
        fetchByAccountId(accountId)?.toApi()

    /**
     * Fetches an [Account] by its name and converts it to the API representation.
     * @param name The name of the account to fetch.
     * @return The [Account] if found, or null if not found.
     */
    suspend fun getByAccountName(name: String) =
        fetchByAccountName(name)?.toApi()

}