package dev.slne.surf.transaction.velocity.commands.transaction.admin.subcommands

import com.github.shynixn.mccoroutine.velocity.launch
import com.velocitypowered.api.proxy.Player
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.doubleArgument
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.surfapi.core.api.generated.SoundKeys
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.util.logger
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

private val log = logger()

fun CommandAPICommand.transactionAddCommand() = subcommand("add") {
    withPermission("surf.transaction.admin.add")

    playerUuidArgument("playerName", showSuggestions = true)
    currencyArgument("currency")
    doubleArgument("amount", min = 1.0)

    playerExecutor { sender, args ->
        val (playerName, uuidDeferred) = args.getUnchecked<PlayerUuidArgumentType>("playerName")
            ?: error("Player UUID argument is missing or invalid")

        val currency: Currency by args
        val amount: Double by args

        plugin.container.launch {
            val uuid = uuidDeferred.await() ?: run {
                sender.sendText {
                    appendPrefix()

                    error("Der Benutzer ")
                    variableValue(playerName)
                    error(" konnte nicht gefunden werden!")
                }

                return@launch
            }

            val user = TransactionUser[uuid]
            val (result) = user.deposit(
                amount,
                currency,
                TransactionData("admin.transaction.add", sender.uniqueId.toString())
            )

            val player = plugin.proxy.getPlayer(uuid).getOrNull()
            when (result) {
                TransactionResult.SUCCESS -> handleSuccess(
                    sender,
                    player,
                    playerName,
                    amount,
                    currency
                )

                TransactionResult.RECEIVER_INSUFFICIENT_FUNDS -> handleError(
                    sender,
                    result,
                    uuid
                )

                TransactionResult.SENDER_INSUFFICIENT_FUNDS -> handleError(
                    sender,
                    result,
                    uuid
                )

                is TransactionResult.DATABASE_ERROR -> handleError(
                    sender,
                    result,
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

    log.atSevere()
        .withCause((result as? TransactionResult.DATABASE_ERROR)?.cause)
        .log("An error occurred when trying to add money to player with UUID $receiverUuid: ${result.message ?: "No message provided"}")
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

            info("Du hast ")
            append(currency.format(amount))
            info(" von ")
            variableValue(sender.username)
            info(" erhalten!")
        }

        player.playSound {
            type(SoundKeys.ENTITY_CHICKEN_EGG)
            volume(0.5f)
            source(Sound.Source.PLAYER)
        }
    }

    sender.sendText {
        appendPrefix()

        darkSpacer("[")
        variableKey("Admin")
        darkSpacer("] ")

        success("Du hast ")
        append(currency.format(amount))
        success(" an ")
        variableValue(playerName)
        success(" gesendet!")
    }
}