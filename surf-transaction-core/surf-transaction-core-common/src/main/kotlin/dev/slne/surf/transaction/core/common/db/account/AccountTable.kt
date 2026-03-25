package dev.slne.surf.transaction.core.common.db.account

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.table.AuditableLongIdTable

object AccountTable : AuditableLongIdTable("accounts") {
    val accountId = nativeUuid("account_id")
    val ownerId = nativeUuid("owner_id")
    val name = varchar("name", 64).uniqueIndex()
    val defaultAccount = bool("default_account").default(false)
}