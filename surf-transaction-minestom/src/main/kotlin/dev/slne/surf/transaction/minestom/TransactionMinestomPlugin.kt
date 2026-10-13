package dev.slne.surf.transaction.minestom

import dev.slne.surf.api.minestom.plugin.SurfMinestomPlugin
import dev.slne.surf.transaction.core.common.TransactionInstance
import dev.slne.surf.transaction.minestom.command.balance.balanceCommand
import dev.slne.surf.transaction.minestom.command.currency.currencyCommand
import dev.slne.surf.transaction.minestom.command.pay.payCommand
import dev.slne.surf.transaction.minestom.command.transaction.transactionCommand

object TransactionMinestomPlugin : SurfMinestomPlugin() {
    override suspend fun onLoad() {
        TransactionInstance.INSTANCE.load()
        TransactionInstance.INSTANCE.enable()
    }

    override suspend fun onEnable() {
        transactionCommand()
        currencyCommand()
        balanceCommand()
        payCommand()
    }

    override suspend fun onDisable() {
        TransactionInstance.INSTANCE.disable()
    }
}
