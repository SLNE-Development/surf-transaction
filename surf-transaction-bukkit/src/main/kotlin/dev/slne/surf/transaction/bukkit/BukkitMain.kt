package dev.slne.surf.transaction.bukkit

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.database.DatabaseProvider
import dev.slne.surf.transaction.core.currency.currencyService
import dev.slne.surf.transaction.fallback.currency.FallbackCurrencyTable
import dev.slne.surf.transaction.fallback.transaction.FallbackTransactionTable
import dev.slne.surf.transaction.fallback.transaction.data.FallbackTransactionDataTable
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.io.path.div

//val plugin get() = BukkitMain.INSTANCE

class BukkitMain : SuspendingJavaPlugin() {

    override suspend fun onLoadAsync() {
        INSTANCE = this

        DatabaseProvider(dataPath, dataPath / "storage").connect()
        transaction {
            SchemaUtils.create(
                FallbackCurrencyTable,
                FallbackTransactionTable,
                FallbackTransactionDataTable
            )

            runBlocking {
                currencyService.fetchCurrencies()
            }
        }
    }

    companion object {
        lateinit var INSTANCE: BukkitMain
    }
}