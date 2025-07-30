package dev.slne.surf.transaction.paper

import com.github.shynixn.mccoroutine.folia.SuspendingJavaPlugin
import dev.slne.surf.transaction.paper.commands.balance.balanceCommand
import dev.slne.surf.transaction.paper.commands.currency.currencyCommand
import dev.slne.surf.transaction.paper.commands.pay.payCommand
import dev.slne.surf.transaction.paper.commands.transaction.transactionCommand
import org.bukkit.plugin.java.JavaPlugin

class PaperMain : SuspendingJavaPlugin() {
    override suspend fun onLoadAsync() {
        transactionCommand()
        currencyCommand()
        balanceCommand()
        payCommand()
    }
}

val plugin get() = JavaPlugin.getPlugin(PaperMain::class.java)