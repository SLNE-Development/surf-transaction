package dev.slne.surf.transaction.paper.server.commands.transaction.admin

import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.transaction.paper.server.commands.CommandPermission
import dev.slne.surf.transaction.paper.server.commands.transaction.admin.subcommands.transactionAddCommand
import dev.slne.surf.transaction.paper.server.commands.transaction.admin.subcommands.transactionRemoveCommand

fun CommandTree.transactionAdminCommand() = literalArgument("admin") {
    withPermission(CommandPermission.TRANSACTION_ADMIN)
    transactionAddCommand()
    transactionRemoveCommand()
}