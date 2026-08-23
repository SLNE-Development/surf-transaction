package dev.slne.surf.transaction.core.common.account.member

import dev.slne.surf.api.core.util.toObjectSet
import dev.slne.surf.transaction.api.account.member.AccountMemberOperations
import dev.slne.surf.transaction.api.account.member.results.AccountMemberResult
import dev.slne.surf.transaction.core.common.account.CoreAccountService
import java.util.*
import java.util.concurrent.atomic.AtomicReference

class AccountMemberOperationsImpl(
    private val accountId: UUID,
    members: Set<UUID>
) : AccountMemberOperations {

    private val memberSnapshot: AtomicReference<Set<UUID>> = AtomicReference(members.toObjectSet())

    override val members: Set<UUID> get() = memberSnapshot.get()

    override suspend fun addMember(
        executor: UUID,
        target: UUID
    ): AccountMemberResult {
        val result = CoreAccountService.addMemberToAccount(accountId, executor, target)
        if (result == AccountMemberResult.SUCCESS) {
            memberSnapshot.updateAndGet { current ->
                if (target in current) current else (current + target).toObjectSet()
            }
        }
        return result
    }

    override suspend fun removeMember(
        executor: UUID,
        target: UUID
    ): AccountMemberResult {
        val result = CoreAccountService.removeMemberFromAccount(accountId, executor, target)
        if (result == AccountMemberResult.SUCCESS) {
            memberSnapshot.updateAndGet { current ->
                if (target in current) (current - target).toObjectSet() else current
            }
        }
        return result
    }

    override fun isMember(player: UUID): Boolean {
        return members.contains(player)
    }
}
