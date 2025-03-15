package dev.slne.surf.transaction.velocity.commands.transaction.admin

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.transaction.velocity.commands.transaction.admin.subcommands.TransactionAddCommand
import dev.slne.surf.transaction.velocity.commands.transaction.admin.subcommands.TransactionRemoveCommand

object TransactionAdminCommand : CommandAPICommand("admin") {

    init {
        withPermission("surf.transaction.admin")

        subcommand(TransactionAddCommand)
        subcommand(TransactionRemoveCommand)
    }

}