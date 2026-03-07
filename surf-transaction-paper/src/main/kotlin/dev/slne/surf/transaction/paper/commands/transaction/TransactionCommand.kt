package dev.slne.surf.transaction.paper.commands.transaction

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.slne.surf.transaction.paper.commands.CommandPermission
import dev.slne.surf.transaction.paper.commands.transaction.admin.transactionAdminCommand


fun transactionCommand() = commandTree("transaction") {
    withPermission(CommandPermission.TRANSACTION)
    transactionAdminCommand()
}