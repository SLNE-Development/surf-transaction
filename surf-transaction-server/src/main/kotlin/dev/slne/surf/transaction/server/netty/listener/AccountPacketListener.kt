package dev.slne.surf.transaction.server.netty.listener

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacketHandler
import dev.slne.surf.transaction.core.netty.packets.clientbound.ClientboundAccountResponsePacket
import dev.slne.surf.transaction.core.netty.packets.clientbound.ClientboundAllAccountsResponsePacket
import dev.slne.surf.transaction.core.netty.packets.clientbound.ClientboundCreateAccountResponsePacket
import dev.slne.surf.transaction.core.netty.packets.clientbound.ClientboundDeleteAccountResponsePacket
import dev.slne.surf.transaction.core.netty.packets.serverbound.*
import dev.slne.surf.transaction.server.account.AccountService
import org.springframework.stereotype.Component

@Component
class AccountPacketListener(
    private val accountService: AccountService
) {
    @SurfNettyPacketHandler
    suspend fun handleAccountByAccountId(packet: ServerboundGetAccountPacket) {
        packet.respond(
            ClientboundAccountResponsePacket(
                accountService.findAccountByAccountId(packet.accountId)
            )
        )
    }

    @SurfNettyPacketHandler
    suspend fun handleDefaultAccount(packet: ServerboundGetDefaultAccountPacket) {
        packet.respond(
            ClientboundAccountResponsePacket(
                accountService.getDefaultAccount(packet.player)
            )
        )
    }

    @SurfNettyPacketHandler
    suspend fun handleCreateAccount(packet: ServerboundCreateAccountPacket) {
        packet.respond(
            ClientboundCreateAccountResponsePacket(
                accountService.createAccount(
                    packet.owner,
                    packet.name,
                    true
                )
            )
        )
    }

    @SurfNettyPacketHandler
    suspend fun handleDeleteAccount(packet: ServerboundDeleteAccountPacket) {
        packet.respond(
            ClientboundDeleteAccountResponsePacket(
                accountService.deleteAccount(packet.accountId)
            )
        )
    }

    @SurfNettyPacketHandler
    suspend fun handleGetAccountByName(packet: ServerboundGetAccountByNamePacket) {
        packet.respond(
            ClientboundAccountResponsePacket(
                accountService.getAccountByName(packet.accountName)
            )
        )
    }

    @SurfNettyPacketHandler
    suspend fun handleGetAllAccounts(packet: ServerboundGetAllAccountsPacket) {
        val accounts = accountService.getAllAccountsByOwner(packet.owner).toSet()

        packet.respond(ClientboundAllAccountsResponsePacket(accounts))
    }
}