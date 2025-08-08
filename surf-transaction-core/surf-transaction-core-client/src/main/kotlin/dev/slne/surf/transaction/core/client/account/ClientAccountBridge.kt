package dev.slne.surf.transaction.core.client.account

import dev.slne.surf.cloud.api.client.netty.packet.fireAndAwaitOrThrow
import dev.slne.surf.transaction.api.account.InternalAccountBridge
import dev.slne.surf.transaction.core.netty.packets.serverbound.ServerboundGetAccountPacket
import org.springframework.stereotype.Component
import java.util.*

@Component
class ClientAccountBridge : InternalAccountBridge {
    override suspend fun getAccountByAccountId(accountId: UUID) =
        ServerboundGetAccountPacket(accountId).fireAndAwaitOrThrow().account
}