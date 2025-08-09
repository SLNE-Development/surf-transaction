package dev.slne.surf.transaction.paper.commands.account.subcommands

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.cloud.api.common.player.toCloudPlayer
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.util.mapAsync
import dev.slne.surf.transaction.api.user.accounts
import dev.slne.surf.transaction.paper.commands.CommandPermission
import dev.slne.surf.transaction.paper.plugin

fun CommandAPICommand.accountListCommand() = subcommand("list") {
    withPermission(CommandPermission.ACCOUNT_LIST)

    playerExecutor { player, args ->
        plugin.launch {
            val cloudPlayer = player.toCloudPlayer() ?: return@launch
            val accounts = cloudPlayer.accounts().mapAsync { it to it.asComponent() }

            cloudPlayer.sendText {
                info("Deine Accounts: ")
                appendCollectionNewLine(accounts) { it.second }
            }
        }
    }
}