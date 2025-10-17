package dev.slne.surf.transaction.server.account.db

import dev.slne.surf.cloud.api.server.exposed.columns.nativeUuid
import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongIdTable

object AccountTable : AuditableLongIdTable("accounts") {
    val accountId = nativeUuid("account_id")
    val ownerId = nativeUuid("owner_id")
    val name = varchar("name", 64).uniqueIndex()
    val defaultAccount = bool("default_account").default(false)
}