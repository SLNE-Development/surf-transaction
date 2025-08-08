package dev.slne.surf.transaction.server

import dev.slne.surf.cloud.api.server.exposed.migration.generateSimpleExposedMigration
import dev.slne.surf.transaction.server.currency.db.CurrencyTable
import dev.slne.surf.transaction.server.transaction.db.TransactionDataTable
import dev.slne.surf.transaction.server.transaction.db.TransactionTable
import org.jetbrains.exposed.sql.ExperimentalDatabaseMigrationApi

@OptIn(ExperimentalDatabaseMigrationApi::class)
fun main() {
    generateSimpleExposedMigration(
        TransactionTable,
        TransactionDataTable,
        CurrencyTable,
        scriptName = "V1__create_transaction_tables",
    )
}