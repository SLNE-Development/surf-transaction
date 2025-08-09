package dev.slne.surf.transaction.server.account.db

import dev.slne.surf.transaction.core.account.AccountImpl
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class AccountEntity(id: EntityID<Long>) : LongEntity(id) {

    companion object : LongEntityClass<AccountEntity>(AccountTable)

    var accountId by AccountTable.accountId
    var owner by AccountTable.owner
    var name by AccountTable.name
    var defaultAccount by AccountTable.defaultAccount

    fun toApi() = AccountImpl(
        accountId = accountId,
        ownerUuid = owner,
        name = name,
        defaultAccount = defaultAccount
    )
}