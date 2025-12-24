package dev.slne.surf.transaction.velocity

import com.google.inject.Inject
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.annotation.DataDirectory
import dev.slne.surf.transaction.core.TransactionInstance
import kotlinx.coroutines.runBlocking
import java.nio.file.Path

lateinit var plugin: VelocityMain

class VelocityMain @Inject constructor(
    @param:DataDirectory val dataPath: Path,
) {
    init {
        plugin = this
        runBlocking {
            TransactionInstance.get().load()
        }
    }

    @Subscribe
    suspend fun onProxyInitialize(event: ProxyInitializeEvent) {
        TransactionInstance.get().enable()
    }

    @Subscribe
    suspend fun onProxyShutdown(event: ProxyShutdownEvent) {
        TransactionInstance.get().disable()
    }
}