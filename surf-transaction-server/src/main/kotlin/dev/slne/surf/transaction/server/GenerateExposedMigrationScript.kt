package dev.slne.surf.transaction.server

import MigrationUtils
import dev.slne.surf.cloud.api.common.config.properties.requiredSystemProperty
import dev.slne.surf.transaction.server.currency.db.CurrencyTable
import dev.slne.surf.transaction.server.transaction.db.TransactionDataTable
import dev.slne.surf.transaction.server.transaction.db.TransactionTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.ExperimentalDatabaseMigrationApi
import org.jetbrains.exposed.sql.transactions.transaction

val database = Database.connect(
    url = requiredSystemProperty("migration", "dbUrl") { it }.value(),
    user = requiredSystemProperty("migration", "dbUser") { it }.value(),
    password = requiredSystemProperty("migration", "dbPassword") { it }.value()
)

@OptIn(ExperimentalDatabaseMigrationApi::class)
fun main() {
    transaction(database) {
        MigrationUtils.generateMigrationScript(
            TransactionTable,
            TransactionDataTable,
            CurrencyTable,
            scriptDirectory = "src/main/resources/db/migration",
            scriptName = "V1__create_transaction_tables",
        )
    }
}