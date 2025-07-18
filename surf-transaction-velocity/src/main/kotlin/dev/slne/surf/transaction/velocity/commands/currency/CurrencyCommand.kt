package dev.slne.surf.transaction.velocity.commands.currency

import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.slne.surf.transaction.velocity.commands.currency.admin.currencyAdminCommand

fun currencyCommand() = commandAPICommand("currency") {
    withPermission("surf.transaction.currency")
    currencyAdminCommand()
}