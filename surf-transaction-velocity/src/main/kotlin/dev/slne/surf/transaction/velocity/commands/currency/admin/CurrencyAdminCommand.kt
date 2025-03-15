package dev.slne.surf.transaction.velocity.commands.currency.admin

import dev.jorel.commandapi.CommandAPICommand
import dev.slne.surf.transaction.velocity.commands.currency.admin.subcommands.CurrencyCreateCommand

object CurrencyAdminCommand : CommandAPICommand("admin") {
    init {
        withPermission("surf.transaction.currency.admin")

        withSubcommand(CurrencyCreateCommand)
    }
}