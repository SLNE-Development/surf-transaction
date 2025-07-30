package dev.slne.surf.transaction.bukkit

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import org.bukkit.plugin.java.JavaPlugin

class BukkitMain : SuspendingJavaPlugin() {
    override suspend fun onLoadAsync() {
    }
}

val plugin get() = JavaPlugin.getPlugin(BukkitMain::class.java)