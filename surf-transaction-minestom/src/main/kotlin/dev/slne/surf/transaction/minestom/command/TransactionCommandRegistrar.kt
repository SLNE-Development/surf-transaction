package dev.slne.surf.transaction.minestom.command

import com.google.inject.Inject
import dev.slne.minestom.lobby.api.command.CommandRegistrar
import dev.slne.surf.transaction.minestom.command.balance.balanceCommand
import dev.slne.surf.transaction.minestom.command.currency.currencyCommand
import dev.slne.surf.transaction.minestom.command.pay.payCommand
import dev.slne.surf.transaction.minestom.command.transaction.transactionCommand

/**
 * Registers the transaction commands of this plugin.
 */
class TransactionCommandRegistrar @Inject constructor() : CommandRegistrar {
    override fun register() {
        transactionCommand()
        currencyCommand()
        balanceCommand()
        payCommand()
    }
}
