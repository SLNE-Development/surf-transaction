package dev.slne.surf.transaction.server

import dev.slne.surf.cloud.api.server.exposed.migration.generateSimpleExposedMigration
import dev.slne.surf.transaction.server.account.db.AccountTable
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
        AccountTable,
        scriptName = "V4__modify_accounts_table",
    )
}