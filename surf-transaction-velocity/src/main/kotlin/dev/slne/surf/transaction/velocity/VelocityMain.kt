package dev.slne.surf.transaction.velocity

import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.google.inject.Inject
import com.velocitypowered.api.plugin.PluginContainer
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import dev.slne.surf.database.DatabaseProvider
import dev.slne.surf.transaction.core.currency.currencyService
import dev.slne.surf.transaction.fallback.currency.FallbackCurrencyTable
import dev.slne.surf.transaction.fallback.transaction.FallbackTransactionTable
import dev.slne.surf.transaction.velocity.commands.TransactionCommand
import dev.slne.surf.transaction.fallback.transaction.data.FallbackTransactionDataTable
import dev.slne.surf.transaction.velocity.commands.transaction.TransactionCommand
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
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

        println("Hello, Velocity!")

        DatabaseProvider(dataPath, dataPath / "storage").connect()
        transaction {
            SchemaUtils.create(
                FallbackCurrencyTable,
                FallbackTransactionTable,
                FallbackTransactionDataTable
            )

//            FallbackCurrency.new {
//                name = "CastCoin"
//                displayName = buildText { success("CastCoin") }
//                symbol = "CC"
//                symbolDisplay = buildText { success("CC") }
//                scale = CurrencyScale.INTEGER
//                defaultCurrency = true
//            }

            runBlocking {
                currencyService.fetchCurrencies()
            }
        }

        TransactionCommand().register()
    }
}