package dev.slne.surf.transaction.minestom

import com.google.auto.service.AutoService
import dev.slne.minestom.lobby.api.coroutine.minestomAsyncScope
import dev.slne.minestom.lobby.api.extension.ConnectionManager
import dev.slne.surf.transaction.core.client.platform.TransactionPlatform
import kotlinx.coroutines.launch
import net.kyori.adventure.audience.Audience
import java.util.UUID

@AutoService(TransactionPlatform::class)
class MinestomTransactionPlatform : TransactionPlatform {

    override fun withOnlinePlayer(uuid: UUID, block: suspend (Audience) -> Unit) {
        val player = ConnectionManager.getOnlinePlayerByUuid(uuid) ?: return

        minestomAsyncScope.launch { block(player) }
    }
}
