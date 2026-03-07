package dev.slne.surf.transaction.paper.server

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.transaction.core.TransactionInstance
import org.bukkit.plugin.java.JavaPlugin

class PaperMain : SuspendingJavaPlugin() {
    override suspend fun onLoadAsync() {
        TransactionInstance.get().load()
    }

    override suspend fun onEnableAsync() {
        TransactionInstance.get().enable()
    }

    override suspend fun onDisableAsync() {
        TransactionInstance.get().disable()
    }
}

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)