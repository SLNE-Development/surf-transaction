package dev.slne.surf.transaction.bukkit

import dev.slne.surf.cloud.api.common.CloudInstance
import dev.slne.surf.cloud.api.common.startSpringApplication
import dev.slne.surf.transaction.SurfTransactionSpringApplication
import dev.slne.surf.transaction.core.transactionApiBridgeImpl
import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap

class PaperBootstrap: PluginBootstrap {
    override fun bootstrap(context: BootstrapContext) {
        transactionApiBridgeImpl.context = CloudInstance.startSpringApplication(SurfTransactionSpringApplication::class)
    }
}