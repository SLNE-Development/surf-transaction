package dev.slne.surf.transaction.velocity

import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.google.inject.Inject
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import dev.slne.surf.transaction.core.currency.currencyService
import dev.slne.surf.transaction.fallback.FallbackManager
import dev.slne.surf.transaction.velocity.commands.balance.BalanceCommand
import dev.slne.surf.transaction.velocity.commands.currency.CurrencyCommand
import dev.slne.surf.transaction.velocity.commands.pay.PayCommand
import dev.slne.surf.transaction.velocity.commands.transaction.TransactionCommand
import kotlinx.coroutines.runBlocking
import java.nio.file.Path
import kotlin.io.path.div

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

        runBlocking {
            FallbackManager.init(dataPath, dataPath / "storage")
            
            currencyService.fetchCurrencies()
        }

        TransactionCommand.register()
        CurrencyCommand.register()
        BalanceCommand.register()
        PayCommand.register();
    }
}