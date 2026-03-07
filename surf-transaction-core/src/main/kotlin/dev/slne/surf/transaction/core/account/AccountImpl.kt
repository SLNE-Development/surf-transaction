package dev.slne.surf.transaction.core.account

import dev.slne.surf.surfapi.core.api.serializer.java.uuid.SerializableStringUUID
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.account.member.AccountMemberOperations
import dev.slne.surf.transaction.api.transactional.CurrencyTransactional
import dev.slne.surf.transaction.core.account.member.AccountMemberOperationsImpl
import dev.slne.surf.transaction.core.component.Components
import dev.slne.surf.transaction.core.transactional.CurrencyTransactionalImpl
import kotlinx.serialization.Serializable

@Serializable
class AccountImpl(
    override val accountId: SerializableStringUUID,
    override val name: String,
    override val ownerUuid: SerializableStringUUID,
    override val defaultAccount: Boolean = false,
    override val members: Set<SerializableStringUUID>
) : Account,
    CurrencyTransactional by CurrencyTransactionalImpl(),
    AccountMemberOperations by AccountMemberOperationsImpl(accountId, members) {

    override suspend fun asComponent() = Components.Account.displayName(this)
}