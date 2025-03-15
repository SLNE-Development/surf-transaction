package dev.slne.surf.transaction.velocity.commands.balance

import com.github.shynixn.mccoroutine.velocity.launch
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transactionApi
import dev.slne.surf.transaction.velocity.commands.arguments.PlayerUuidArgumentType
import dev.slne.surf.transaction.velocity.commands.arguments.currencyArgument
import dev.slne.surf.transaction.velocity.commands.arguments.playerUuidArgument
import dev.slne.surf.transaction.velocity.plugin
import kotlinx.coroutines.CompletableDeferred
import kotlin.jvm.optionals.getOrNull

object BalanceCommand : CommandAPICommand("balance") {
    init {
        withPermission("surf.transaction.balance")
        withAliases("bal", "konto", "money")

        currencyArgument("currency")
        playerUuidArgument("playerName", optional = true)

        playerExecutor { sender, args ->
            val currency: Currency by args
            val suppliedPlayer =
                args.getOptionalUnchecked<PlayerUuidArgumentType>("playerName").getOrNull()
                    ?: PlayerUuidArgumentType(
                        sender.username, CompletableDeferred(sender.uniqueId)
                    )


            plugin.container.launch {
                val uuid = suppliedPlayer.second.await() ?: run {
                    sender.sendText {
                        appendPrefix()

                        error("Der Benutzer konnte nicht gefunden werden!")
                    }
                    return@launch
                }

                val user = transactionApi.getTransactionUser(uuid)
                val balance = user.balanceDecimal(currency)

                sender.sendText {
                    appendPrefix()

                    if (user.uuid == sender.uniqueId) {
                        info("Dein Kontostand beträgt ")
                    } else {
                        info("Der Kontostand von ")
                        variableValue(suppliedPlayer.first)
                        info(" beträgt ")
                    }

                    append(currency.format(balance))
                    info(".")
                }
            }
        }
    }
}