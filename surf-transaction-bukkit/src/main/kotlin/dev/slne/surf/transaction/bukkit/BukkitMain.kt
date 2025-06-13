package dev.slne.surf.transaction.bukkit

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.transaction.core.currency.currencyService
import dev.slne.surf.transaction.fallback.FallbackManager
import kotlinx.coroutines.runBlocking
import kotlin.io.path.div

class BukkitMain : SuspendingJavaPlugin() {

    override suspend fun onLoadAsync() {
        INSTANCE = this

        FallbackManager.init(dataPath, dataPath / "storage")

        runBlocking {
            currencyService.fetchCurrencies()
        }
    }

    companion object {
        lateinit var INSTANCE: BukkitMain
    }
}