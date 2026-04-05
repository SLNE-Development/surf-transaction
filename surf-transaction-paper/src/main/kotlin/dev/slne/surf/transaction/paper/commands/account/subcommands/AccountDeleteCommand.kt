package dev.slne.surf.transaction.paper.commands.account.subcommands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.api.core.command.args.awaiting
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.user.transactionUser
import dev.slne.surf.transaction.core.common.component.Components
import dev.slne.surf.transaction.paper.commands.CommandPermission
import dev.slne.surf.transaction.paper.commands.account.arguments.accountArgument

fun CommandAPICommand.accountDeleteCommand() = subcommand("delete") {
    withPermission(CommandPermission.ACCOUNT_DELETE)

    accountArgument("account")

    playerExecutorSuspend { player, args ->
        val account = args.awaiting<Account>("account")
        val result = player.transactionUser().deleteAccount(account)

        player.sendText {
            append(Components.Account.formatDeletionResult(result))
        }
    }
}