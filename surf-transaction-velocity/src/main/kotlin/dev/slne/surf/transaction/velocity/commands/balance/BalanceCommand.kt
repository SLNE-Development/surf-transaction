package dev.slne.surf.transaction.velocity.commands.balance

import com.github.shynixn.mccoroutine.velocity.launch
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.cloud.api.client.velocity.command.args.offlineCloudPlayerArgument
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.player.toOfflineCloudPlayer
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.user.balanceDecimal
import dev.slne.surf.transaction.velocity.commands.arguments.currencyArgument
import dev.slne.surf.transaction.velocity.plugin
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred

fun balanceCommand() = commandAPICommand("balance") {
    withPermission("surf.transaction.balance")
    withAliases("bal", "konto", "money")

    currencyArgument("currency")
    offlineCloudPlayerArgument("player", optional = true)

    playerExecutor { sender, args ->
        val currency: Currency by args
        val playerDeferred: Deferred<OfflineCloudPlayer?> =
            args.getOrDefaultUnchecked("player", CompletableDeferred(sender.toOfflineCloudPlayer()))

        plugin.container.launch {
            val player = playerDeferred.await() ?: return@launch
            val balance = player.balanceDecimal(currency)

            sender.sendText {
                appendPrefix()

                if (player.uuid == sender.uniqueId) {
                    info("Dein Kontostand beträgt ")
                } else {
                    info("Der Kontostand von ")
                    variableValue(name)
                    info(" beträgt ")
                }

                append(currency.format(balance))
                info(".")
            }
        }
    }
}