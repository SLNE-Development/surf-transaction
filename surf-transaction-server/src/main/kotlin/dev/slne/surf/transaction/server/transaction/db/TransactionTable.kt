package dev.slne.surf.transaction.server.transaction.db

import dev.slne.surf.cloud.api.server.exposed.columns.nativeUuid
import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongIdTable
import dev.slne.surf.transaction.server.account.db.AccountTable
import dev.slne.surf.transaction.server.currency.db.CurrencyTable
import org.jetbrains.exposed.sql.ReferenceOption

object TransactionTable : AuditableLongIdTable("transactions") {
    val identifier = nativeUuid("identifier").uniqueIndex()
    val initiator = nativeUuid("initiator_id").nullable()
    val sender = optReference(
        "sender_account", AccountTable,
        onUpdate = ReferenceOption.CASCADE,
        onDelete = ReferenceOption.CASCADE
    )
    val receiver = optReference(
        "receiver_account", AccountTable,
        onUpdate = ReferenceOption.CASCADE,
        onDelete = ReferenceOption.CASCADE
    )
    val currency = reference(
        "currency", CurrencyTable,
        onUpdate = ReferenceOption.CASCADE,
        onDelete = ReferenceOption.CASCADE
    )
    val amount = decimal("amount", 20, 10)
}