package dev.slne.surf.transaction.velocity.commands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.transaction.velocity.commands.admin.TransactionAdminCommand

class TransactionCommand : CommandAPICommand("transaction") {
    init {
        withPermission("surf.transaction")

        subcommand(TransactionAdminCommand())
    }
}