package dev.slne.surf.transaction.server.transaction.db

import org.jetbrains.exposed.dao.id.LongIdTable

object TransactionDataTable: LongIdTable("transaction_transaction_data"){

    val transaction = reference("transaction", TransactionTable)
    val key = varchar("data_key", 255)
    val value = largeText("data_value")
}