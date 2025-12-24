package dev.slne.surf.transaction.core.db

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.transaction.core.db.account.AccountMemberTable
import dev.slne.surf.transaction.core.db.account.AccountTable
import dev.slne.surf.transaction.core.db.currency.CurrencyTable
import dev.slne.surf.transaction.core.db.transaction.TransactionDataTable
import dev.slne.surf.transaction.core.db.transaction.TransactionTable

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