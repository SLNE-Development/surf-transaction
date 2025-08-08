package dev.slne.surf.transaction.server.netty.listener

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacketHandler
import dev.slne.surf.transaction.core.netty.packets.clientbound.ClientboundAccountResponsePacket
import dev.slne.surf.transaction.core.netty.packets.serverbound.ServerboundGetAccountPacket
import dev.slne.surf.transaction.core.netty.packets.serverbound.ServerboundGetDefaultAccountPacket
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
}