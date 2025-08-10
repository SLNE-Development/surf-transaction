package dev.slne.surf.transaction.server.account.db

import dev.slne.surf.cloud.api.server.exposed.columns.nativeUuid
import org.jetbrains.exposed.dao.id.LongIdTable

object AccountTable : LongIdTable("transaction_accounts") {
    val accountId = nativeUuid("account_id")
    val owner = nativeUuid("owner_id")
    val name = varchar("name", 64).uniqueIndex()
    val defaultAccount = bool("default_account").default(false)
}