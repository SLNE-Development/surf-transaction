package dev.slne.surf.transaction.core.db.item

import dev.slne.surf.database.table.AuditableLongIdTable

object ItemFingerprintTable : AuditableLongIdTable("item_fingerprints") {
    val hash = varchar("hash", 64).uniqueIndex()
    val itemType = varchar("item_type", 255)
    val componentData = largeText("component_data").nullable()
    val serializedTemplate = binary("serialized_template")
    val dataVersion = integer("data_version")
}