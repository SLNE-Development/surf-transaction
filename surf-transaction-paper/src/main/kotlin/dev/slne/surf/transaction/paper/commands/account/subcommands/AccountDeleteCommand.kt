package dev.slne.surf.transaction.paper.commands.account.subcommands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.surfapi.bukkit.api.command.executors.playerExecutorSuspend
import dev.slne.surf.surfapi.core.api.command.args.awaiting
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.user.transactionUser
import dev.slne.surf.transaction.core.component.Components
import dev.slne.surf.transaction.paper.commands.CommandPermission
import dev.slne.surf.transaction.paper.commands.account.arguments.accountArgument

fun CommandAPICommand.accountDeleteCommand() = subcommand("delete") {
    withPermission(CommandPermission.ACCOUNT_DELETE)

    accountArgument("account")

    playerExecutorSuspend { player, args ->
        val account = args.awaiting<Account>("account")
        val result = player.transactionUser().deleteAccount(account)

        player.sendText {
            appendPrefix()
            append(Components.Account.formatDeletionResult(result))
        }
    }
}