package dev.slne.surf.transaction.velocity.commands.transaction.admin

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.transaction.velocity.commands.transaction.admin.subcommands.transactionAddCommand
import dev.slne.surf.transaction.velocity.commands.transaction.admin.subcommands.transactionRemoveCommand

fun CommandAPICommand.transactionAdminCommand() = subcommand("admin") {
    withPermission("surf.transaction.admin")
    transactionAddCommand()
    transactionRemoveCommand()
}