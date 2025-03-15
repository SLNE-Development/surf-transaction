package dev.slne.surf.transaction.velocity.commands.arguments

import com.github.shynixn.mccoroutine.velocity.launch
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CommandAPIArgumentType
import dev.jorel.commandapi.executors.CommandArguments
import dev.slne.surf.surfapi.core.api.service.PlayerLookupService
import dev.slne.surf.transaction.velocity.plugin
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import java.util.*
import kotlin.jvm.optionals.getOrNull

typealias PlayerUuidArgumentType = Pair<String, Deferred<UUID?>>

/**
 * Argument for a player uuid
 *
 * @param nodeName the name of the node for this argument
 * @param showSuggestions whether to show suggestions for this argument
 */
class PlayerUuidArgument(nodeName: String, showSuggestions: Boolean = false) :
    Argument<PlayerUuidArgumentType>(nodeName, StringArgumentType.word()) {

    init {
        if (showSuggestions) {
            replaceSuggestions(ArgumentSuggestions.stringCollection { _ ->
                plugin.proxy.getAllPlayers().map { it.username }
            })
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun getPrimitiveType(): Class<PlayerUuidArgumentType> {
        return Deferred::class.java as Class<PlayerUuidArgumentType>
    }

    override fun getArgumentType(): CommandAPIArgumentType {
        return CommandAPIArgumentType.PRIMITIVE_STRING
    }

    override fun <Source : Any> parseArgument(
        cmdCtx: CommandContext<Source>,
        key: String?,
        previousArgs: CommandArguments
    ): PlayerUuidArgumentType {
        val playerString = StringArgumentType.getString(cmdCtx, key)
        val player = plugin.proxy.getPlayer(playerString).getOrNull()

        if (player != null) {
            return playerString to CompletableDeferred(player.uniqueId)
        }

        val deferrable = CompletableDeferred<UUID?>()

        plugin.container.launch {
            deferrable.complete(PlayerLookupService.getUuid(playerString))
        }

        return playerString to deferrable
    }
}

/**
 * Adds a player uuid argument to the command
 *
 * @param nodeName the name of the node for this argument
 * @param showSuggestions whether to show suggestions for this argument
 * @param optional whether the argument is optional
 * @param block the configuration of the argument
 *
 * @return the command
 */
inline fun CommandAPICommand.playerUuidArgument(
    nodeName: String,
    showSuggestions: Boolean = false,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandAPICommand = withArguments(
    PlayerUuidArgument(nodeName, showSuggestions).setOptional(optional).apply(block)
)