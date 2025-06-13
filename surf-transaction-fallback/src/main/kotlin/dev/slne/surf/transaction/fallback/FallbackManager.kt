package dev.slne.surf.transaction.fallback

import dev.slne.surf.database.DatabaseManager
import dev.slne.surf.transaction.fallback.currency.FallbackCurrencyTable
import dev.slne.surf.transaction.fallback.transaction.FallbackTransactionTable
import dev.slne.surf.transaction.fallback.transaction.data.FallbackTransactionDataTable
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.nio.file.Path

object FallbackManager {

    private lateinit var databaseManager: DatabaseManager

    suspend fun init(configPath: Path, storagePath: Path) {
        databaseManager = DatabaseManager(configPath, storagePath)
        databaseManager.databaseProvider.connect()

        newSuspendedTransaction {
            SchemaUtils.create(
                FallbackCurrencyTable,
                FallbackTransactionTable,
                FallbackTransactionDataTable
            )
        }
    }

    fun disconnect() {
        databaseManager.databaseProvider.disconnect()
    }

}