package dev.slne.surf.transaction.velocity.commands.transaction

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.transaction.velocity.commands.transaction.admin.TransactionAdminCommand
import dev.slne.surf.transaction.velocity.commands.transaction.admin.transactionAdminCommand


fun transactionCommand() = commandAPICommand("transaction") {
    withPermission("surf.transaction")
    transactionAdminCommand()
}