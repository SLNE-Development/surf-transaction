package dev.slne.surf.transaction.velocity.commands.admin.subcommands

import com.github.shynixn.mccoroutine.velocity.launch
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.doubleArgument
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.stringArgument
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.transaction.api.transactionApi
import dev.slne.surf.transaction.api.user.TransactionUser
import dev.slne.surf.transaction.velocity.TestData
import dev.slne.surf.transaction.velocity.plugin
import kotlin.jvm.optionals.getOrNull

class TransactionAddCommand : CommandAPICommand("add") {

    init {
        withPermission("surf.transaction.admin.add")

        stringArgument("playerName") {
            replaceSuggestions(ArgumentSuggestions.stringCollection { _ ->
                plugin.proxy.allPlayers.map { it.username }
            })
        }

        doubleArgument("amount", min = 1.0)

        anyExecutor { commandSource, args ->
            val playerName: String by args
            val amount: Double by args

            val player = plugin.proxy.getPlayer(playerName).getOrNull() ?: run {
                commandSource.sendText {
                    error("Player not found")
                }

                return@anyExecutor
            }

            val currency = transactionApi.getCurrencyByName("CastCoin") ?: run {
                commandSource.sendText {
                    error("Currency not found")
                }

                return@anyExecutor
            }

            val user = TransactionUser.get(player.uniqueId)
            plugin.container.launch {
                user.deposit(amount, currency, TestData("admin-add"))

                commandSource.sendText {
                    success("Successfully added $amount ${currency.name} to ${player.username}")
                }
            }

        }
    }
}