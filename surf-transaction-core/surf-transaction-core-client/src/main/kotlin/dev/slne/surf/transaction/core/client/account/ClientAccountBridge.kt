package dev.slne.surf.transaction.core.client.account

import dev.slne.surf.cloud.api.client.netty.packet.fireAndAwaitOrThrow
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.util.toObjectSet
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.core.account.CommonAccountBridge
import dev.slne.surf.transaction.core.netty.packets.serverbound.*
import org.springframework.stereotype.Component
import java.util.*

@Component
class ClientAccountBridge : CommonAccountBridge() {
    override suspend fun getAccountByAccountId(accountId: UUID) =
        ServerboundGetAccountPacket(accountId).fireAndAwaitOrThrow().account

    override suspend fun getAllAccountsByOwner(owner: OfflineCloudPlayer) =
        ServerboundGetAllAccountsPacket(owner).fireAndAwaitOrThrow().accounts.toObjectSet()
    
    override suspend fun getDefaultAccountOrNull(player: OfflineCloudPlayer): Account? =
        ServerboundGetDefaultAccountPacket(player).fireAndAwaitOrThrow().account

    override suspend fun createAccount(
        owner: OfflineCloudPlayer,
        name: String
    ) = ServerboundCreateAccountPacket(
        owner,
        name
    ).fireAndAwaitOrThrow().result

    override suspend fun getAccountByName(
        name: String
    ) = ServerboundGetAccountByNamePacket(
        name
    ).fireAndAwaitOrThrow().account

    override suspend fun getAccounts(player: OfflineCloudPlayer) =
        ServerboundGetAllAccountsPacket(player).fireAndAwaitOrThrow().accounts.toObjectSet()

    override suspend fun deleteAccount(
        account: Account
    ) = ServerboundDeleteAccountPacket(account.accountId).fireAndAwaitOrThrow().result
}