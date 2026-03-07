package dev.slne.surf.transaction.paper.server.commands.currency

import dev.jorel.commandapi.kotlindsl.commandTree
import dev.slne.surf.transaction.paper.server.commands.CommandPermission
import dev.slne.surf.transaction.paper.server.commands.currency.admin.currencyAdminCommand

fun currencyCommand() = commandTree("currency") {
    withPermission(CommandPermission.CURRENCY)
    currencyAdminCommand()
}