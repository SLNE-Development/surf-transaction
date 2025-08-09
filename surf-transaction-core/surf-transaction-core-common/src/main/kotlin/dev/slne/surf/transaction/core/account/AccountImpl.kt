package dev.slne.surf.transaction.core.account

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.player.toOfflineCloudPlayer
import dev.slne.surf.surfapi.core.api.messages.adventure.appendNewline
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.transaction.api.account.Account
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class AccountImpl(
    override val accountId: @Contextual UUID,
    val ownerUuid: @Contextual UUID,
    override val name: String,
    override val defaultAccount: Boolean = false
) : Account {
    override val owner: OfflineCloudPlayer
        get() = ownerUuid.toOfflineCloudPlayer()

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