package dev.slne.surf.transaction.core.account

import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.account.member.AccountMemberOperations
import dev.slne.surf.transaction.api.transactional.Transactional
import dev.slne.surf.transaction.core.account.member.AccountMemberOperationsImpl
import dev.slne.surf.transaction.core.component.Components
import dev.slne.surf.transaction.core.transactional.TransactionalImpl
import java.util.*

class AccountImpl(
    override val accountId: UUID,
    override val name: String,
    override val ownerUuid: UUID,
    override val defaultAccount: Boolean = false,
    override val members: Set<UUID>
) : Account,
    Transactional by TransactionalImpl(),
    AccountMemberOperations by AccountMemberOperationsImpl(accountId, members) {

    override suspend fun asComponent() = Components.Account.displayName(this)
}