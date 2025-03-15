package dev.slne.surf.transaction.fallback.transaction.data

import dev.slne.surf.transaction.fallback.transaction.FallbackTransactionTable
import org.jetbrains.exposed.dao.id.LongIdTable

object FallbackTransactionDataTable : LongIdTable("transaction_transaction_data") {

    val transaction = reference("transaction", FallbackTransactionTable)
    val key = varchar("data_key", 255)
    val value = largeText("data_value")
    
}
