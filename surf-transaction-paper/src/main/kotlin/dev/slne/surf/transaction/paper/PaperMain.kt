package dev.slne.surf.transaction.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.transaction.core.common.TransactionInstance
import org.bukkit.plugin.java.JavaPlugin

class PaperMain : SuspendingJavaPlugin() {
    override suspend fun onLoadAsync() {
        TransactionInstance.INSTANCE.load()
    }

    override suspend fun onEnableAsync() {
        TransactionInstance.INSTANCE.enable()
    }

    override suspend fun onDisableAsync() {
        TransactionInstance.INSTANCE.disable()
    }
}

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)