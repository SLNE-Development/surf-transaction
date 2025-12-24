package dev.slne.surf.transaction.core.db.transaction

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ReferenceOption
import dev.slne.surf.database.table.AuditableLongIdTable
import dev.slne.surf.transaction.core.db.account.AccountTable
import dev.slne.surf.transaction.core.db.currency.CurrencyTable

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