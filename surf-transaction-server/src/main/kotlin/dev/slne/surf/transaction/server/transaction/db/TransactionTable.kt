package dev.slne.surf.transaction.server.transaction.db

import dev.slne.surf.cloud.api.server.exposed.columns.nativeUuid
import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongIdTable
import dev.slne.surf.transaction.server.currency.db.CurrencyTable

object TransactionTable: AuditableLongIdTable("transaction_transactions") {
    val identifier = nativeUuid("identifier").uniqueIndex()
    val sender = nativeUuid("sender").nullable()
    val receiver = nativeUuid("receiver").nullable()
    val currency = reference("currency", CurrencyTable)
    val amount = decimal("amount", 20, 10)
}