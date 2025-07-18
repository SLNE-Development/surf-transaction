package dev.slne.surf.transaction.velocity.commands.currency.admin

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.transaction.velocity.commands.currency.admin.subcommands.currencyCreateCommand
import dev.slne.surf.transaction.velocity.commands.currency.admin.subcommands.currencyMakeDefaultCommand

fun CommandAPICommand.currencyAdminCommand() = subcommand("admin") {
    withPermission("surf.transaction.currency.admin")
    currencyCreateCommand()
    currencyMakeDefaultCommand()
}