package dev.slne.surf.transaction.core.common.account

import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.account.member.AccountMemberOperations
import dev.slne.surf.transaction.api.transactional.Transactional
import dev.slne.surf.transaction.core.common.account.member.AccountMemberOperationsImpl
import dev.slne.surf.transaction.core.common.component.Components
import dev.slne.surf.transaction.core.common.transactional.TransactionalImpl
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class AccountImpl(
    override val accountId: @Contextual UUID,
    override val name: String,
    override val ownerUuid: @Contextual UUID,
    override val defaultAccount: Boolean = false,
    override val members: Set<@Contextual UUID>
) : Account,
    Transactional by TransactionalImpl(),
    AccountMemberOperations by AccountMemberOperationsImpl(accountId, members) {

    override suspend fun asComponent() = Components.Account.displayName(this)
}