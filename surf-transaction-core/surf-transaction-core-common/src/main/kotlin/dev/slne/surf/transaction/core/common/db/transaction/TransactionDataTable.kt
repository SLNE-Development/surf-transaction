package dev.slne.surf.transaction.core.common.db.transaction

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ReferenceOption
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.dao.id.ULongIdTable

object TransactionDataTable : ULongIdTable("transaction_data") {
    val transaction = reference(
        "transaction_id", TransactionTable,
        onUpdate = ReferenceOption.CASCADE,
        onDelete = ReferenceOption.CASCADE
    )
    val key = varchar("data_key", 255)
    val value = largeText("data_value")
}