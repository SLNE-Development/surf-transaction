package dev.slne.surf.transaction.minestom.command.transaction.admin

import dev.slne.minestom.lobby.api.command.commandapi.CommandTree
import dev.slne.minestom.lobby.api.command.commandapi.dsl.literalArgument
import dev.slne.surf.transaction.core.client.command.TransactionPermissions
import dev.slne.surf.transaction.minestom.command.transaction.admin.subcommands.transactionAddCommand
import dev.slne.surf.transaction.minestom.command.transaction.admin.subcommands.transactionRemoveCommand

fun CommandTree.transactionAdminCommand() = literalArgument("admin") {
    withPermission(TransactionPermissions.TRANSACTION_ADMIN)
    transactionAddCommand()
    transactionRemoveCommand()
}
