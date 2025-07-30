package dev.slne.surf.transaction.paper.commands.balance

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.cloud.api.client.paper.command.args.offlineCloudPlayerArgument
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.player.toOfflineCloudPlayer
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.user.balance
import dev.slne.surf.transaction.paper.commands.CommandPermission
import dev.slne.surf.transaction.paper.commands.arguments.currencyArgument
import dev.slne.surf.transaction.paper.plugin
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

fun balanceCommand() = commandTree("balance") {
    withPermission(CommandPermission.BALANCE)
    withAliases("bal", "konto", "money")

    currencyArgument("currency") {
        playerExecutor { sender, args ->
            balance(
                sender,
                args.getUnchecked("currency")!!,
                CompletableDeferred(sender.toOfflineCloudPlayer())
            )
        }

        offlineCloudPlayerArgument("player") {
            anyExecutor { sender, args ->
                balance(
                    sender,
                    args.getUnchecked("currency")!!,
                    args.getUnchecked("player")!!
                )
            }
        }
    }
}

private fun balance(
    sender: CommandSender,
    currency: Currency,
    playerDeferred: Deferred<OfflineCloudPlayer?>
) = plugin.launch {
    val player = playerDeferred.await() ?: return@launch
    val balance = player.balance(currency)

    sender.sendText {
        appendPrefix()

        if (sender is Player && player.uuid == sender.uniqueId) {
            info("Dein Kontostand beträgt ")
        } else {
            info("Der Kontostand von ")
            append(player.displayName())
            info(" beträgt ")
        }

        append(currency.format(balance))
        info(".")
    }
}