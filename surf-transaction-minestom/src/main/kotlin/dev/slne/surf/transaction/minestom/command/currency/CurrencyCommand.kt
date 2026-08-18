package dev.slne.surf.transaction.minestom.command.currency

import dev.slne.minestom.lobby.api.command.commandapi.dsl.commandTree
import dev.slne.surf.transaction.core.client.command.TransactionPermissions
import dev.slne.surf.transaction.minestom.command.currency.admin.currencyAdminCommand

fun currencyCommand() = commandTree("currency") {
    withPermission(TransactionPermissions.CURRENCY)
    currencyAdminCommand()
}
