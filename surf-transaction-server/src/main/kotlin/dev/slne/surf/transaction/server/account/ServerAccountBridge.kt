package dev.slne.surf.transaction.server.account

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.core.account.CommonAccountBridge
import org.springframework.stereotype.Component
import java.util.*

@Component
class ServerAccountBridge(private val accountService: AccountService) : CommonAccountBridge() {
    override suspend fun getAccountByAccountId(accountId: UUID) =
        accountService.findAccountByAccountId(accountId)

    override suspend fun createAccount(owner: OfflineCloudPlayer, name: String) =
        accountService.createAccount(owner, name, false)

    override suspend fun getAllAccountsByOwner(owner: OfflineCloudPlayer) =
        accountService.getAllAccountsByOwner(owner)
    
    override suspend fun getAccounts(player: OfflineCloudPlayer) =
        accountService.getAllAccountsByOwner(player)

    override suspend fun getDefaultAccountOrNull(player: OfflineCloudPlayer) =
        accountService.getDefaultAccount(player)

    override suspend fun getAccountByName(name: String) =
        accountService.getAccountByName(name)

    override suspend fun deleteAccount(account: Account) =
        accountService.deleteAccount(account.accountId)
}