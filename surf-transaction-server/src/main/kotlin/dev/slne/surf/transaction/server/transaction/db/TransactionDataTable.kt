package dev.slne.surf.transaction.server.transaction.db

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object TransactionDataTable : LongIdTable("transaction_data") {

    val transaction = reference(
        "transaction", TransactionTable,
        onUpdate = ReferenceOption.CASCADE,
        onDelete = ReferenceOption.CASCADE
    )
    val key = varchar("data_key", 255)
    val value = largeText("data_value")
}