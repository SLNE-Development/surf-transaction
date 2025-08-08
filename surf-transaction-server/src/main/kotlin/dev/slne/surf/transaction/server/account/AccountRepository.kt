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

    suspend fun fetchByPlayer(player: OfflineCloudPlayer): AccountEntity? =
        AccountEntity.find { AccountTable.owner eq player.uuid }.singleOrNull()

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
     * Fetches an [Account] by its account ID and converts it to the API representation.
     *
     * @param accountId The UUID of the account to fetch.
     * @return The [Account] if found, or null if not found.
     */
    suspend fun getByAccountId(accountId: UUID) =
        fetchByAccountId(accountId)?.toApi()

}