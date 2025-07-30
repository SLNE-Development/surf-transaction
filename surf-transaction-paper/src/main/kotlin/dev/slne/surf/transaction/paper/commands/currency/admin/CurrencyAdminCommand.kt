package dev.slne.surf.transaction.paper.commands.currency.admin

import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.transaction.paper.commands.CommandPermission
import dev.slne.surf.transaction.paper.commands.currency.admin.subcommands.currencyCreateCommand
import dev.slne.surf.transaction.paper.commands.currency.admin.subcommands.currencyMakeDefaultCommand
import dev.slne.surf.transaction.paper.commands.currency.admin.subcommands.listCurrenciesCommand

fun CommandTree.currencyAdminCommand() = literalArgument("admin") {
    withPermission(CommandPermission.CURRENCY_ADMIN)
    currencyCreateCommand()
    currencyMakeDefaultCommand()
    listCurrenciesCommand()
}