package dev.slne.surf.transaction.paper.commands.account.subcommands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.util.mapAsync
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.transaction.api.user.transactionUser
import dev.slne.surf.transaction.paper.commands.CommandPermission

fun CommandAPICommand.accountListCommand() = subcommand("list") {
    withPermission(CommandPermission.ACCOUNT_LIST)

    playerExecutorSuspend { player, args ->
        val accounts = player.transactionUser()
            .getAllAccounts()
            .mapAsync { it to it.asComponent() }

        player.sendText {
            appendInfoPrefix()
            info("Deine Accounts: ")
            appendCollectionNewLine(accounts) { it.second }
        }
    }
}