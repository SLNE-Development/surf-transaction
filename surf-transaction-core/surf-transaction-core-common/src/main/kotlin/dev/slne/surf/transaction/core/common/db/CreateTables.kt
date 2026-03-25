package dev.slne.surf.transaction.core.common.db

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.SchemaUtils
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.transactions.suspendTransaction
import dev.slne.surf.transaction.core.common.db.account.AccountMemberTable
import dev.slne.surf.transaction.core.common.db.account.AccountTable
import dev.slne.surf.transaction.core.common.db.currency.CurrencyTable
import dev.slne.surf.transaction.core.common.db.transaction.TransactionDataTable
import dev.slne.surf.transaction.core.common.db.transaction.TransactionTable

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