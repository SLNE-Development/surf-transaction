package dev.slne.surf.transaction.core.common.account.member

import dev.slne.surf.surfapi.core.api.util.freeze
import dev.slne.surf.surfapi.core.api.util.toMutableObjectSet
import dev.slne.surf.transaction.api.account.member.AccountMemberOperations
import dev.slne.surf.transaction.api.account.member.results.AccountMemberResult
import dev.slne.surf.transaction.core.common.account.AccountServiceImpl
import java.util.*

class AccountMemberOperationsImpl(
    private val accountId: UUID,
    members: Set<UUID>
) : AccountMemberOperations {
    private val _members = members.toMutableObjectSet()
    override val members: Set<UUID> = _members.freeze()

    override suspend fun addMember(
        executor: UUID,
        target: UUID
    ): AccountMemberResult {
        val result = AccountServiceImpl.Companion.get().addMemberToAccount(accountId, executor, target)
        if (result == AccountMemberResult.SUCCESS) {
            _members.add(target)
        }
        return result
    }

    override suspend fun removeMember(
        executor: UUID,
        target: UUID
    ): AccountMemberResult {
        val result = AccountServiceImpl.Companion.get().removeMemberFromAccount(accountId, executor, target)
        if (result == AccountMemberResult.SUCCESS) {
            _members.remove(target)
        }
        return result
    }

    override fun isMember(player: UUID): Boolean {
        return members.contains(player)
    }
}