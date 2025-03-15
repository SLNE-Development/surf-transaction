package dev.slne.surf.transaction.velocity.commands.transaction

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.transaction.velocity.commands.transaction.admin.TransactionAdminCommand

object TransactionCommand : CommandAPICommand("transaction") {
    init {
        withPermission("surf.transaction")

        subcommand(TransactionAdminCommand)
    }
}