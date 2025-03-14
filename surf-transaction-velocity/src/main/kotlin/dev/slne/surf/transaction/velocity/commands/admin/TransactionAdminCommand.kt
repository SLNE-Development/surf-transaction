package dev.slne.surf.transaction.velocity.commands.admin

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.transaction.velocity.commands.admin.subcommands.TransactionAddCommand

class TransactionAdminCommand : CommandAPICommand("admin") {

    init {
        withPermission("surf.transaction.admin")

        subcommand(TransactionAddCommand())
    }

}