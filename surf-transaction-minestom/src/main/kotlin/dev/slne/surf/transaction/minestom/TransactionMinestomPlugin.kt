package dev.slne.surf.transaction.minestom

import com.google.auto.service.AutoService
import dev.slne.minestom.lobby.api.plugin.MinestomPlugin
import dev.slne.minestom.lobby.api.plugin.annotation.MinestomPluginMeta
import dev.slne.surf.transaction.minestom.command.TransactionCommandRegistrar

@AutoService(MinestomPlugin::class)
@MinestomPluginMeta(
    "surf-transaction-minestom",
    dependsOn = [
        "surf-api-minestom",
        "surf-rabbitmq-minestom",
        "surf-redis-minestom",
        "surf-core-minestom"
    ]
)
class TransactionMinestomPlugin : MinestomPlugin(TransactionMinestomEntrypoint::class.java) {
    override fun configurePlugin() {
        bindCommandRegistrar<TransactionCommandRegistrar>()
    }
}
