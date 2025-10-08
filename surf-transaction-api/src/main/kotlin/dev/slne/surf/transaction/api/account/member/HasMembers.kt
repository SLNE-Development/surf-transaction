package dev.slne.surf.transaction.api.account.member

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.transaction.api.util.ComponentResult
import it.unimi.dsi.fastutil.objects.ObjectSet

interface HasMembers {
    /**
     * A set of members associated with the account.
     */
    val members: ObjectSet<OfflineCloudPlayer>

    /**
     * Adds a new member to the account.
     *
     * @param executor The [OfflineCloudPlayer] executing the addition.
     * @param target The [OfflineCloudPlayer] to be added as a member.
     */
    suspend fun addMember(
        executor: OfflineCloudPlayer,
        target: OfflineCloudPlayer
    ): ComponentResult

    /**
     * Removes a member from the account.
     *
     * @param executor The [OfflineCloudPlayer] executing the removal.
     * @param target The [OfflineCloudPlayer] to be removed from the members.
     */
    suspend fun removeMember(
        executor: OfflineCloudPlayer,
        target: OfflineCloudPlayer
    ): ComponentResult

    /**
     * Checks if a player is a member of the account.
     *
     * @param player The [OfflineCloudPlayer] to check for membership.
     * @return `true` if the player is a member, `false` otherwise.
     */
    fun isMember(player: OfflineCloudPlayer): Boolean
}