package dev.slne.surf.transaction.microservice.db.account

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ReferenceOption
import dev.slne.surf.database.table.AuditableLongIdTable

object AccountMemberTable : AuditableLongIdTable("account_members") {
    val accountId = reference(
        "account_id",
        AccountTable,
        onDelete = ReferenceOption.CASCADE,
        onUpdate = ReferenceOption.CASCADE
    )

    val memberId = nativeUuid("member_id")

    init {
        uniqueIndex(accountId, memberId)
    }
}