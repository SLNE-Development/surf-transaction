package dev.slne.surf.transaction.minestom.command

import dev.slne.surf.api.minestom.command.CommandAPI
import dev.slne.surf.api.minestom.command.executor.CommandArguments
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.core.api.common.player.SurfPlayer
import kotlinx.coroutines.Deferred
import java.util.UUID

/**
 * Awaits the offline player the argument [nodeName] was parsed into.
 *
 * @throws dev.slne.surf.api.minestom.command.exception.CommandSyntaxException
 * if no player is known under the given name
 */
suspend fun CommandArguments.awaitSurfOfflinePlayerUuid(nodeName: String): UUID =
    get<Deferred<SurfPlayer?>>(nodeName).await()?.uuid
        ?: CommandAPI.failWithMessage(
            buildText {
                appendErrorPrefix()
                error("Der Spieler wurde nicht gefunden.")
            }
        )
