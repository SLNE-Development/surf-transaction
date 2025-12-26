package dev.slne.surf.transaction.api.account.member

import dev.slne.surf.transaction.api.account.member.results.AccountMemberResult
import java.util.*

interface AccountMemberOperations {
    val members: Set<UUID>

    suspend fun addMember(
        executor: UUID,
        target: UUID
    ): AccountMemberResult

    suspend fun removeMember(
        executor: UUID,
        target: UUID
    ): AccountMemberResult

    fun isMember(player: UUID): Boolean
}