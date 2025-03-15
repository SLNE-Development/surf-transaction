package dev.slne.surf.transaction.velocity.commands.transaction.admin.subcommands

import com.github.shynixn.mccoroutine.velocity.launch
import com.velocitypowered.api.proxy.Player
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.doubleArgument
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.surfapi.core.api.generated.SoundKeys
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.user.TransactionUser
import dev.slne.surf.transaction.velocity.commands.arguments.PlayerUuidArgumentType
import dev.slne.surf.transaction.velocity.commands.arguments.currencyArgument
import dev.slne.surf.transaction.velocity.commands.arguments.playerUuidArgument
import dev.slne.surf.transaction.velocity.plugin
import net.kyori.adventure.sound.Sound
import java.util.*
import kotlin.jvm.optionals.getOrNull

object TransactionRemoveCommand : CommandAPICommand("remove") {

    init {
        withPermission("surf.transaction.admin.remove")

        playerUuidArgument("playerName", showSuggestions = true)
        currencyArgument("currency")
        doubleArgument("amount", min = 1.0)

        playerExecutor { sender, args ->
            val suppliedPlayer: PlayerUuidArgumentType = args.getUnchecked("playerName") ?: run {
                sender.sendText {
                    appendPrefix()

                    error("Der Benutzer konnte nicht gefunden werden!")
                }

                return@playerExecutor
            }

            val currency: Currency by args
            val amount: Double by args

            if (amount <= 0) {
                sender.sendText {
                    appendPrefix()

                    error("Der Betrag muss größer als 0 sein!")
                }
            }

            plugin.container.launch {
                val playerName = suppliedPlayer.first
                val uuid = suppliedPlayer.second.await() ?: run {
                    sender.sendText {
                        appendPrefix()

                        error("Der Benutzer ")
                        variableValue(playerName)
                        error(" konnte nicht gefunden werden!")
                    }

                    return@launch
                }

                val user = TransactionUser.get(uuid)
                val result = user.withdraw(
                    amount,
                    currency,
                    TransactionData("admin.transaction.remove", sender.uniqueId.toString())
                )

                val player = plugin.proxy.getPlayer(uuid).getOrNull()

                when (result.first) {
                    TransactionResult.SUCCESS -> handleSuccess(
                        sender,
                        player,
                        playerName,
                        amount,
                        currency
                    )

                    TransactionResult.RECEIVER_INSUFFICIENT_FUNDS -> handleError(
                        sender,
                        result.first,
                        uuid
                    )

                    TransactionResult.SENDER_INSUFFICIENT_FUNDS -> handleError(
                        sender,
                        result.first,
                        uuid
                    )

                    TransactionResult.DATABASE_ERROR -> handleError(
                        sender,
                        result.first,
                        uuid
                    )
                }
            }

        }
    }

    private fun handleError(sender: Player, result: TransactionResult, receiverUuid: UUID) {
        sender.sendText {
            appendPrefix()

            error("Es ist ein Fehler aufgetreten!")
        }

        error("An error occurred when trying to remove money from player with UUID $receiverUuid. Result: $result")
    }

    private fun handleSuccess(
        sender: Player,
        player: Player?,
        playerName: String,
        amount: Double,
        currency: Currency
    ) {
        if (player != null) {
            player.sendText {
                appendPrefix()

                darkSpacer("[")
                variableKey("Admin")
                darkSpacer("] ")

                variableValue(sender.username)
                info(" hat dir ")
                variableValue(amount.toString())
                info(" ")
                append(currency.symbolDisplay)
                info(" abgezogen!")
            }

            player.playSound {
                type(SoundKeys.ENTITY_CHICKEN_EGG)
                volume(.5f)
                source(Sound.Source.PLAYER)
            }
        }

        sender.sendText {
            appendPrefix()

            darkSpacer("[")
            variableKey("Admin")
            darkSpacer("] ")

            success("Du hast ")
            variableValue(amount.toString())
            success(" ")
            append(currency.symbolDisplay)
            success(" von ")
            variableValue(playerName)
            success(" abgezogen!")
        }
    }
}