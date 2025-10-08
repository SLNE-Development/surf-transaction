package dev.slne.surf.transaction.server.account.db.member

import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongEntity
import dev.slne.surf.transaction.server.account.db.AccountEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class AccountMemberEntity(id: EntityID<Long>) : AuditableLongEntity(id, AccountMemberTable) {
    companion object : LongEntityClass<AccountMemberEntity>(AccountMemberTable)

    var memberId by AccountMemberTable.memberId
    var accountId by AccountEntity referencedOn AccountMemberTable.accountId
}