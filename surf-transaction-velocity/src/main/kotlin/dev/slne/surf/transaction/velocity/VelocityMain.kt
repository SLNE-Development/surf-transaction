package dev.slne.surf.transaction.velocity

import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.google.inject.Inject
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import dev.slne.surf.cloud.api.common.CloudInstance
import dev.slne.surf.cloud.api.common.startSpringApplication
import dev.slne.surf.transaction.SurfTransactionSpringApplication
import dev.slne.surf.transaction.core.netty.packets.ClientboundRefreshCurrencies
import dev.slne.surf.transaction.core.transactionApiBridgeImpl
import dev.slne.surf.transaction.velocity.commands.balance.balanceCommand
import dev.slne.surf.transaction.velocity.commands.currency.currencyCommand
import dev.slne.surf.transaction.velocity.commands.pay.payCommand
import dev.slne.surf.transaction.velocity.commands.transaction.transactionCommand
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

        println("org/springframework/scheduling/annotation/ProxyAsyncConfiguration: " + Class.forName("org.springframework.scheduling.annotation.ProxyAsyncConfiguration"))
        println("ClientboundRefreshCurrencies serializer: " + ClientboundRefreshCurrencies.serializer())

        transactionApiBridgeImpl.context =
            CloudInstance.startSpringApplication(SurfTransactionSpringApplication::class)

        transactionCommand()
        currencyCommand()
        balanceCommand()
        payCommand()
    }
}