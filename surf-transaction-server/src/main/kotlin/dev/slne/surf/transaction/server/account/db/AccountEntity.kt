package dev.slne.surf.transaction.server.account.db

import dev.slne.surf.transaction.core.account.AccountImpl
import dev.slne.surf.transaction.server.account.db.member.AccountMemberEntity
import dev.slne.surf.transaction.server.account.db.member.AccountMemberTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class AccountEntity(id: EntityID<Long>) : LongEntity(id) {

    companion object : LongEntityClass<AccountEntity>(AccountTable)

    var accountId by AccountTable.accountId
    var ownerId by AccountTable.ownerId
    var name by AccountTable.name
    var defaultAccount by AccountTable.defaultAccount

    val members by AccountMemberEntity referrersOn AccountMemberTable.accountId

    fun toApi() = AccountImpl(
        accountId = accountId,
        ownerUuid = ownerId,
        memberUuidList = members.map { it.memberId }.toList(),
        name = name,
        defaultAccount = defaultAccount,
    )
}