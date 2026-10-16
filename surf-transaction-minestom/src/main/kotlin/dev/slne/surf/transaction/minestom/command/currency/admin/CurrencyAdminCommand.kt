package dev.slne.surf.transaction.minestom.command.currency.admin

import dev.slne.surf.api.minestom.command.CommandTree
import dev.slne.surf.api.minestom.command.dsl.literalArgument
import dev.slne.surf.transaction.core.client.command.TransactionPermissions
import dev.slne.surf.transaction.minestom.command.currency.admin.subcommands.currencyCreateCommand
import dev.slne.surf.transaction.minestom.command.currency.admin.subcommands.currencyMakeDefaultCommand
import dev.slne.surf.transaction.minestom.command.currency.admin.subcommands.listCurrenciesCommand

fun CommandTree.currencyAdminCommand() = literalArgument("admin") {
    withPermission(TransactionPermissions.CURRENCY_ADMIN)
    currencyCreateCommand()
    currencyMakeDefaultCommand()
    listCurrenciesCommand()
}
