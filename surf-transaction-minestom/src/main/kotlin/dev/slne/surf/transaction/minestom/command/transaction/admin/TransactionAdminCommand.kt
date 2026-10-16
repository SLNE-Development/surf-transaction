package dev.slne.surf.transaction.minestom.command.transaction.admin

import dev.slne.surf.api.minestom.command.CommandTree
import dev.slne.surf.api.minestom.command.dsl.literalArgument
import dev.slne.surf.transaction.core.client.command.TransactionPermissions
import dev.slne.surf.transaction.minestom.command.transaction.admin.subcommands.transactionAddCommand
import dev.slne.surf.transaction.minestom.command.transaction.admin.subcommands.transactionRemoveCommand

fun CommandTree.transactionAdminCommand() = literalArgument("admin") {
    withPermission(TransactionPermissions.TRANSACTION_ADMIN)
    transactionAddCommand()
    transactionRemoveCommand()
}
