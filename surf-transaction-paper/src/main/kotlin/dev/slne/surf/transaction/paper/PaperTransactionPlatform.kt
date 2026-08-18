package dev.slne.surf.transaction.paper

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.google.auto.service.AutoService
import dev.slne.surf.api.paper.extensions.server
import dev.slne.surf.transaction.core.client.platform.TransactionPlatform
import net.kyori.adventure.audience.Audience
import java.util.UUID

@AutoService(TransactionPlatform::class)
class PaperTransactionPlatform : TransactionPlatform {

    override fun withOnlinePlayer(uuid: UUID, block: suspend (Audience) -> Unit) {
        val player = server.getPlayer(uuid) ?: return

        plugin.launch(plugin.entityDispatcher(player)) { block(player) }
    }
}
