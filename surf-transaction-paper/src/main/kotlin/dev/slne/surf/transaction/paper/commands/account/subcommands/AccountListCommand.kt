package dev.slne.surf.transaction.paper.commands.account.subcommands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.surfapi.bukkit.api.command.executors.playerExecutorSuspend
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.util.mapAsync
import dev.slne.surf.transaction.api.user.transactionUser
import dev.slne.surf.transaction.paper.commands.CommandPermission

fun CommandAPICommand.accountListCommand() = subcommand("list") {
    withPermission(CommandPermission.ACCOUNT_LIST)

    playerExecutorSuspend { player, args ->
        val accounts = player.transactionUser()
            .getAllAccounts()
            .mapAsync { it to it.asComponent() }

        player.sendText {
            info("Deine Accounts: ")
            appendCollectionNewLine(accounts) { it.second }
        }
    }
}