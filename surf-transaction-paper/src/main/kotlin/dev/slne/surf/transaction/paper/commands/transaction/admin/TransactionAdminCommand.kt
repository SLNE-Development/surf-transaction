package dev.slne.surf.transaction.paper.commands.transaction.admin

import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.transaction.paper.commands.CommandPermission
import dev.slne.surf.transaction.paper.commands.transaction.admin.subcommands.transactionAddCommand
import dev.slne.surf.transaction.paper.commands.transaction.admin.subcommands.transactionRemoveCommand

fun CommandTree.transactionAdminCommand() = literalArgument("admin") {
    withPermission(CommandPermission.TRANSACTION_ADMIN)
    transactionAddCommand()
    transactionRemoveCommand()
}