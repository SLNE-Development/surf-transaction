package dev.slne.surf.transaction.velocity.commands.admin.subcommands

import com.github.shynixn.mccoroutine.velocity.launch
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.doubleArgument
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.stringArgument
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.service.PlayerLookupService
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.transactionApi
import dev.slne.surf.transaction.api.user.TransactionUser
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

            plugin.container.launch {
                val uuid =
                    plugin.proxy.getPlayer(playerName).getOrNull()?.uniqueId
                        ?: PlayerLookupService.getUuid(playerName) ?: run {
                            commandSource.sendText {
                                error("Player $playerName not found")
                            }

                            return@launch
                        }


                val currency = transactionApi.getCurrencyByName("CastCoin") ?: run {
                    commandSource.sendText {
                        error("Currency not found")
                    }

                    return@launch
                }

                val user = TransactionUser.get(uuid)
                user.deposit(amount, currency, TransactionData("test.message", "Dies ist ein Test"))

                commandSource.sendText {
                    success("Successfully added $amount ${currency.name} to $playerName")
                }
            }

        }
    }
}