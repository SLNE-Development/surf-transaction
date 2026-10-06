package dev.slne.surf.transaction.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.api.paper.hook.papi.SurfPaperPAPIHook
import dev.slne.surf.transaction.core.common.TransactionInstance
import dev.slne.surf.transaction.paper.papi.PapiExpansion
import org.bukkit.plugin.java.JavaPlugin

class PaperMain : SuspendingJavaPlugin() {
    override suspend fun onLoadAsync() {
        TransactionInstance.INSTANCE.load()
    }

    override suspend fun onEnableAsync() {
        TransactionInstance.INSTANCE.enable()
        SurfPaperPAPIHook.register(PapiExpansion)
    }

    override suspend fun onDisableAsync() {
        TransactionInstance.INSTANCE.disable()
    }
}

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)