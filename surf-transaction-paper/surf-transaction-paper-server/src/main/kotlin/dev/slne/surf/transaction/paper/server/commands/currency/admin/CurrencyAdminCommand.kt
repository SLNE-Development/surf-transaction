package dev.slne.surf.transaction.paper.server.commands.currency.admin

import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.transaction.paper.server.commands.CommandPermission
import dev.slne.surf.transaction.paper.server.commands.currency.admin.subcommands.currencyCreateCommand
import dev.slne.surf.transaction.paper.server.commands.currency.admin.subcommands.currencyMakeDefaultCommand
import dev.slne.surf.transaction.paper.server.commands.currency.admin.subcommands.listCurrenciesCommand

fun CommandTree.currencyAdminCommand() = literalArgument("admin") {
    withPermission(CommandPermission.CURRENCY_ADMIN)
    currencyCreateCommand()
    currencyMakeDefaultCommand()
    listCurrenciesCommand()
}