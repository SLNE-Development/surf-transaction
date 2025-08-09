package dev.slne.surf.transaction.paper.commands.account.subcommands

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.stringArgument
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.cloud.api.common.player.toCloudPlayer
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.transaction.api.user.createAccount
import dev.slne.surf.transaction.paper.commands.CommandPermission
import dev.slne.surf.transaction.paper.plugin

fun CommandAPICommand.accountCreateCommand() = subcommand("create") {
    withPermission(CommandPermission.ACCOUNT_CREATE)

    stringArgument("name")

    playerExecutor { player, args ->
        val name: String by args

        plugin.launch {
            val cloudPlayer = player.toCloudPlayer() ?: return@launch
            val result = cloudPlayer.createAccount(name)
            val message = result.message()

            cloudPlayer.sendText {
                appendPrefix()
                append(message)
            }
        }
    }
}