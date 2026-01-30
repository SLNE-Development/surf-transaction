package dev.slne.surf.transaction.paper.server.commands

import dev.slne.surf.transaction.paper.server.commands.balance.balanceCommand
import dev.slne.surf.transaction.paper.server.commands.currency.currencyCommand
import dev.slne.surf.transaction.paper.server.commands.pay.payCommand
import dev.slne.surf.transaction.paper.server.commands.transaction.transactionCommand

class CommandManager {
    fun registerCommands() {
        transactionCommand()
        currencyCommand()
        balanceCommand()
        payCommand()
//        accountCommand()
    }
}