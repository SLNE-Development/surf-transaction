package dev.slne.surf.transaction.velocity

import com.github.shynixn.mccoroutine.velocity.launch
import com.google.auto.service.AutoService
import dev.slne.surf.transaction.core.client.platform.TransactionPlatform
import net.kyori.adventure.audience.Audience
import java.util.UUID

@AutoService(TransactionPlatform::class)
class VelocityTransactionPlatform : TransactionPlatform {

    override fun withOnlinePlayer(uuid: UUID, block: suspend (Audience) -> Unit) {
        val player = plugin.proxy.getPlayer(uuid).orElse(null) ?: return

        plugin.container.launch { block(player) }
    }
}
