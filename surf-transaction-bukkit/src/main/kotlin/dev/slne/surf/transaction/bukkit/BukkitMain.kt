package dev.slne.surf.transaction.bukkit

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.transaction.core.currency.CurrencyService
import dev.slne.surf.transaction.fallback.FallbackManager
import kotlinx.coroutines.runBlocking
import org.bukkit.plugin.java.JavaPlugin
import kotlin.io.path.div

class BukkitMain : SuspendingJavaPlugin() {
    override suspend fun onLoadAsync() {

        FallbackManager.init(dataPath, dataPath / "storage")

        runBlocking {
            CurrencyService.fetchCurrencies()
        }
    }
}

val plugin get() = JavaPlugin.getPlugin(BukkitMain::class.java)