package dev.slne.surf.transaction.minestom.command.transaction

import dev.slne.minestom.lobby.api.command.commandapi.dsl.commandTree
import dev.slne.surf.transaction.core.client.command.TransactionPermissions
import dev.slne.surf.transaction.minestom.command.transaction.admin.transactionAdminCommand


fun transactionCommand() = commandTree("transaction") {
    withPermission(TransactionPermissions.TRANSACTION)
    transactionAdminCommand()
}
