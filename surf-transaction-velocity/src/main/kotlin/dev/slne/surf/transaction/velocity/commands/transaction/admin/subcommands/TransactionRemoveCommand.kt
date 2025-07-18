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

fun CommandAPICommand.transactionRemoveCommand() = subcommand("remove") {
    withPermission("surf.transaction.admin.remove")

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
            val (result) = user.withdraw(
                amount,
                currency,
                ignoreMinimum = true,
                TransactionData("admin.transaction.remove", sender.uniqueId.toString())
            )

            val player = plugin.proxy.getPlayer(uuid).getOrNull()

            if (result == TransactionResult.SUCCESS) {
                handleSuccess(sender, player, playerName, amount, currency)
            } else {
                handleError(sender, result, uuid)
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
        .log("An error occurred when trying to remove money from player with UUID $receiverUuid: ${result.message ?: "No message provided"}")
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
            append(currency.format(amount.toBigDecimal()))
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
        append(currency.format(amount.toBigDecimal()))
        success(" von ")
        variableValue(playerName)
        success(" abgezogen!")
    }
}