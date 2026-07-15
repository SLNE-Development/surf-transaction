package dev.slne.surf.transaction.microservice.db

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.transaction.microservice.db.account.AccountMemberTable
import dev.slne.surf.transaction.microservice.db.account.AccountTable
import dev.slne.surf.transaction.microservice.db.currency.CurrencyTable
import dev.slne.surf.transaction.microservice.db.transaction.table.TransactionDataTable
import dev.slne.surf.transaction.microservice.db.transaction.table.TransactionTable

object CreateTables {
    suspend fun create() = suspendTransaction {
        SchemaUtils.create(
            AccountTable,
            AccountMemberTable,
            CurrencyTable,
            TransactionTable,
            TransactionDataTable
        )
    }
}
