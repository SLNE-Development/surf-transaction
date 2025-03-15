package dev.slne.surf.transaction.velocity.commands.arguments

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.velocitypowered.api.command.VelocityBrigadierMessage
import com.velocitypowered.api.proxy.Player
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.CommandAPIArgumentType
import dev.jorel.commandapi.executors.CommandArguments
import dev.slne.surf.surfapi.core.api.messages.adventure.text
import dev.slne.surf.transaction.velocity.plugin
import kotlin.jvm.optionals.getOrNull

/**
 * Argument for a player
 *
 * @param nodeName the name of the node for this argument
 */
open class PlayerArgument(nodeName: String) :
    Argument<Player>(nodeName, StringArgumentType.word()) {

    override fun getPrimitiveType(): Class<Player> {
        return Player::class.java
    }

    override fun getArgumentType(): CommandAPIArgumentType {
        return CommandAPIArgumentType.PRIMITIVE_STRING
    }

    override fun <Source : Any> parseArgument(
        cmdCtx: CommandContext<Source>,
        key: String?,
        previousArgs: CommandArguments
    ): Player {
        val playerString = StringArgumentType.getString(cmdCtx, key)

        return plugin.proxy.getPlayer(playerString).getOrNull() ?: throw SimpleCommandExceptionType(
            VelocityBrigadierMessage.tooltip(text("Player $playerString not found or not online"))
        ).create()
    }
}

/**
 * Adds a player argument to the command
 *
 * @param nodeName the name of the node for this argument
 * @param optional whether the argument is optional
 * @param block the configuration of the argument
 *
 * @return the command
 */
inline fun CommandAPICommand.playerArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandAPICommand = withArguments(
    PlayerArgument(nodeName).setOptional(optional).apply(block)
)