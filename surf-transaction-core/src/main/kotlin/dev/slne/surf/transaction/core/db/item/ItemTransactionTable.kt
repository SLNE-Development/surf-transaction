package dev.slne.surf.transaction.core.db.item

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ReferenceOption
import dev.slne.surf.database.table.AuditableLongIdTable

object ItemTransactionTable : AuditableLongIdTable("item_transactions") {
    val identifier = nativeUuid("identifier").uniqueIndex()
    val initiator = nativeUuid("initiator_id").nullable()

    val sender = optReference(
        "sender_account", ItemAccountTable,
        onUpdate = ReferenceOption.CASCADE,
        onDelete = ReferenceOption.CASCADE
    )
    val receiver = optReference(
        "receiver_account", ItemAccountTable,
        onUpdate = ReferenceOption.CASCADE,
        onDelete = ReferenceOption.CASCADE
    )

    val amount = integer("amount")
    val fingerprint = reference(
        "fingerprint_id", ItemFingerprintTable,
        onUpdate = ReferenceOption.CASCADE,
        onDelete = ReferenceOption.CASCADE
    )
}