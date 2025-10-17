package dev.slne.surf.transaction.core.account

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.player.toOfflineCloudPlayer
import dev.slne.surf.cloud.api.common.util.freeze
import dev.slne.surf.cloud.api.common.util.toObjectSet
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.account.InternalAccountBridge
import dev.slne.surf.transaction.api.account.member.results.AccountMemberResult
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.*

@Serializable
data class AccountImpl(
    override val accountId: @Contextual UUID,
    val ownerUuid: @Contextual UUID,
    private val memberUuidList: List<@Contextual UUID>,
    override val name: String,
    override val defaultAccount: Boolean = false
) : Account {

    override val owner: OfflineCloudPlayer
        get() = ownerUuid.toOfflineCloudPlayer()

    @Transient
    private val _members = memberUuidList.map { it.toOfflineCloudPlayer() }.toObjectSet()
    override val members get() = _members.freeze()

    override suspend fun addMember(
        executor: OfflineCloudPlayer,
        target: OfflineCloudPlayer
    ): AccountMemberResult {
        if (isMember(target)) {
            return AccountMemberResult.AlreadyMember(
                accountId = accountId,
                executorUuid = executor.uuid,
                targetUuid = target.uuid
            )
        }

        return InternalAccountBridge.instance.addMemberToAccount(
            account = this,
            executor = executor,
            target = target
        )
    }

    override suspend fun removeMember(
        executor: OfflineCloudPlayer,
        target: OfflineCloudPlayer
    ): AccountMemberResult {
        if (!isMember(target)) {
            return AccountMemberResult.NotMember(
                accountId = accountId,
                executorUuid = executor.uuid,
                targetUuid = target.uuid
            )
        }

        return InternalAccountBridge.instance.removeMemberFromAccount(
            account = this,
            executor = executor,
            target = target
        )
    }

    override fun isMember(player: OfflineCloudPlayer) = _members.contains(player)

    override suspend fun asComponent() = buildText {
        variableValue(name)

        hoverEvent(buildText {
            variableKey("Account Id: ")
            variableValue(accountId.toString())
            appendNewline(2)

            variableKey("Besitzer: ")
            append(owner.displayName())
            appendNewline(2)

            variableKey("Standardkonto: ")
            if (defaultAccount) {
                success("Ja")
            } else {
                error("Nein")
            }
        })
    }
}