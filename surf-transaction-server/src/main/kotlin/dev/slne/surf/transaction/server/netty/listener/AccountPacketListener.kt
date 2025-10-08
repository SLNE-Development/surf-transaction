package dev.slne.surf.transaction.server.netty.listener

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacketHandler
import dev.slne.surf.transaction.core.netty.packets.clientbound.*
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
    suspend fun handleAddMemberToAccountPacket(packet: ServerboundAddMemberToAccountPacket) {
        packet.respond(
            ClientboundAddMemberToAccountResultPacket(
                accountId = packet.accountId,
                executorId = packet.executorId,
                targetId = packet.targetId,
                result = accountService.addMemberToAccount(
                    packet.accountId,
                    packet.executorId,
                    packet.targetId
                )
            )
        )
    }

    @SurfNettyPacketHandler
    suspend fun handleRemoveMemberFromAccountPacket(packet: ServerboundRemoveMemberFromAccountPacket) {
        packet.respond(
            ClientboundRemoveMemberFromAccountResultPacket(
                accountId = packet.accountId,
                executorId = packet.executorId,
                targetId = packet.targetId,
                result = accountService.removeMemberFromAccount(
                    packet.accountId,
                    packet.executorId,
                    packet.targetId
                )
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
                    false
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