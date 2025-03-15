package dev.slne.surf.transaction.velocity.commands.currency

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.transaction.velocity.commands.currency.admin.CurrencyAdminCommand

object CurrencyCommand : CommandAPICommand("currency") {
    init {
        withPermission("surf.transaction.currency")

        subcommand(CurrencyAdminCommand)
    }
}