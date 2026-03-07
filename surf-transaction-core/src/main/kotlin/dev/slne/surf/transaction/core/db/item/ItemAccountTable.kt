package dev.slne.surf.transaction.core.db.item

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ReferenceOption
import dev.slne.surf.database.table.AuditableLongIdTable

object ItemAccountTable : AuditableLongIdTable("item_accounts") {
    val accountId = nativeUuid("account_id").uniqueIndex()
    val ownerId = nativeUuid("owner_id")
    val fingerprint = reference(
        "fingerprint_id", ItemFingerprintTable,
        onUpdate = ReferenceOption.CASCADE,
        onDelete = ReferenceOption.CASCADE
    )

    val name = varchar("name", 128).nullable()
}