package dev.slne.surf.transaction.paper.commands.account.subcommands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.stringArgument
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.transaction.api.user.transactionUser
import dev.slne.surf.transaction.core.common.component.Components
import dev.slne.surf.transaction.paper.commands.CommandPermission

fun CommandAPICommand.accountCreateCommand() = subcommand("create") {
    withPermission(CommandPermission.ACCOUNT_CREATE)

    stringArgument("name")

    playerExecutorSuspend { player, args ->
        val name: String by args
        val result = player.transactionUser().createAccount(name)

        player.sendText {
            appendInfoPrefix()
            append(Components.Account.formatCreationResult(result))
        }
    }
}