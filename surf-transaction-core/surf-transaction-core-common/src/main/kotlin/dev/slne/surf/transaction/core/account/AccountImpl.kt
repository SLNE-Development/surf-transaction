package dev.slne.surf.transaction.core.account

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.player.toOfflineCloudPlayer
import dev.slne.surf.transaction.api.account.Account
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class AccountImpl(
    override val accountId: @Contextual UUID,
    val ownerUuid: @Contextual UUID,
    override val name: String
) : Account {
    override val owner: OfflineCloudPlayer
        get() = ownerUuid.toOfflineCloudPlayer()
}