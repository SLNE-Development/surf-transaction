package dev.slne.surf.transaction.server.account

import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.server.plugin.CoroutineTransactional
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.server.account.db.AccountEntity
import dev.slne.surf.transaction.server.account.db.AccountTable
import org.springframework.stereotype.Repository
import java.util.*

@CoroutineTransactional
@Repository
class AccountRepository {

    /**
     * Create a new account for the given [OfflineCloudPlayer] with the specified name.
     *
     * @param owner The [OfflineCloudPlayer] who will own the account.
     * @param name The name of the account to be created.
     *
     * @return The newly created [Account].
     */
    suspend fun createAccount(
        owner: OfflineCloudPlayer,
        name: String
    ) = AccountEntity.new {
        this.owner = owner.uuid
        this.accountId = UUID.randomUUID()
        this.name = name
    }.toApi()

    /**
     * Get the default account for a player
     *
     * @param player The [OfflineCloudPlayer] to get the default account for
     *
     * @return The default [Account] for the player
     */
    suspend fun getDefaultAccount(
        player: OfflineCloudPlayer
    ) = fetchByPlayer(player)?.toApi() ?: run {
        val cloudPlayer = player.player ?: return@run null

        createByPlayer(cloudPlayer).toApi()
    }

    /**
     * Fetches an [AccountEntity] by the owner [OfflineCloudPlayer].
     *
     * @param player The [OfflineCloudPlayer] whose account is being fetched.
     * @return The [AccountEntity] if found, or null if not found.
     */
    suspend fun fetchByPlayer(player: OfflineCloudPlayer): AccountEntity? =
        AccountEntity.find { AccountTable.owner eq player.uuid }.singleOrNull()

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