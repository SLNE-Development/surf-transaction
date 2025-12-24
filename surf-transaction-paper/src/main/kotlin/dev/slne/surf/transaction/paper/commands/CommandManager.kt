package dev.slne.surf.transaction.paper.commands

import dev.slne.surf.transaction.paper.commands.balance.balanceCommand
import dev.slne.surf.transaction.paper.commands.currency.currencyCommand
import dev.slne.surf.transaction.paper.commands.pay.payCommand
import dev.slne.surf.transaction.paper.commands.transaction.transactionCommand

class CommandManager {
    fun registerCommands() {
        transactionCommand()
        currencyCommand()
        balanceCommand()
        payCommand()
//        accountCommand()
    }
}