package dev.slne.surf.transaction.api.account.member

import dev.slne.surf.transaction.api.account.member.results.AccountMemberResult
import org.jetbrains.annotations.ApiStatus
import java.util.*
/**
 * Defines member management operations for an account.
 *
 * Implementations of this interface allow accounts to manage additional members
 * who may have access to the account, depending on permission rules enforced
 * by the implementation.
 */
@ApiStatus.NonExtendable
interface AccountMemberOperations {
    /**
     * The set of UUIDs representing all members of the account.
     *
     * This does not necessarily include the account owner.
     */
    val members: Set<UUID>

    /**
     * Adds a new member to the account.
     *
     * @param executor the UUID of the entity performing the operation
     * @param target the UUID of the player to be added as a member
     *
     * @return the result of the member addition attempt
     */
    suspend fun addMember(
        executor: UUID,
        target: UUID
    ): AccountMemberResult

    /**
     * Removes a member from the account.
     *
     * @param executor the UUID of the entity performing the operation
     * @param target the UUID of the player to be removed from the account
     *
     * @return the result of the member removal attempt
     */
    suspend fun removeMember(
        executor: UUID,
        target: UUID
    ): AccountMemberResult

    /**
     * Checks whether the given player is a member of the account.
     *
     * @param player the UUID of the player to check
     * @return `true` if the player is a member, otherwise `false`
     */
    fun isMember(player: UUID): Boolean
}