package dev.slne.surf.transaction.paper.commands.account

import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.slne.surf.transaction.paper.commands.CommandPermission
import dev.slne.surf.transaction.paper.commands.account.subcommands.accountCreateCommand
import dev.slne.surf.transaction.paper.commands.account.subcommands.accountDeleteCommand
import dev.slne.surf.transaction.paper.commands.account.subcommands.accountListCommand

fun accountCommand() = commandAPICommand("account") {
    withPermission(CommandPermission.ACCOUNT)

    accountListCommand()
    accountCreateCommand()
    accountDeleteCommand()
}