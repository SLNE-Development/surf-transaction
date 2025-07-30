package dev.slne.surf.transaction.server

import dev.slne.surf.cloud.api.common.CloudInstance
import dev.slne.surf.cloud.api.common.startSpringApplication
import dev.slne.surf.cloud.api.server.plugin.bootstrap.BootstrapContext
import dev.slne.surf.cloud.api.server.plugin.bootstrap.StandalonePluginBootstrap
import dev.slne.surf.transaction.SurfTransactionSpringApplication
import dev.slne.surf.transaction.core.transactionApiBridgeImpl

class ServerTransactionBootstrap : StandalonePluginBootstrap {
    override suspend fun bootstrap(context: BootstrapContext) {
        transactionApiBridgeImpl.context =
            CloudInstance.startSpringApplication(SurfTransactionSpringApplication::class)
    }
}