package dev.slne.surf.transaction.velocity.commands.transaction.admin

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.transaction.velocity.commands.transaction.admin.subcommands.TransactionRemoveCommand
import dev.slne.surf.transaction.velocity.commands.transaction.admin.subcommands.transactionAddCommand

fun CommandAPICommand.transactionAdminCommand() = subcommand("admin") {
    withPermission("surf.transaction.admin")
    transactionAddCommand()
}

object TransactionAdminCommand : CommandAPICommand("admin") {

    init {
        withPermission("surf.transaction.admin")

        subcommand(TransactionRemoveCommand)
    }

}