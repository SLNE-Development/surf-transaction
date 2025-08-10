package dev.slne.surf.transaction.velocity

import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.google.inject.Inject
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import dev.slne.surf.cloud.api.common.CloudInstance
import dev.slne.surf.cloud.api.common.startSpringApplication
import dev.slne.surf.transaction.SurfTransactionSpringApplication
import dev.slne.surf.transaction.core.transactionApiBridgeImpl
import java.nio.file.Path

lateinit var plugin: VelocityMain

class VelocityMain @Inject constructor(
    @DataDirectory private val dataPath: Path,
    val proxy: ProxyServer,
    val container: PluginContainer,
    suspendingPluginContainer: SuspendingPluginContainer
) {

    init {
        plugin = this
        suspendingPluginContainer.initialize(this)
        transactionApiBridgeImpl.context =
            CloudInstance.startSpringApplication(SurfTransactionSpringApplication::class)
    }
}