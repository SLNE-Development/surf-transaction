package dev.slne.surf.transaction.server.account.db.member

import dev.slne.surf.cloud.api.server.exposed.columns.nativeUuid
import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongIdTable
import dev.slne.surf.transaction.server.account.db.AccountTable
import org.jetbrains.exposed.sql.ReferenceOption

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