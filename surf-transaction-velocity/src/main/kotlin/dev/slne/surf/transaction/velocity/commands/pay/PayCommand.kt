package dev.slne.surf.transaction.velocity.commands.pay

import com.github.shynixn.mccoroutine.velocity.launch
import com.velocitypowered.api.proxy.Player
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.doubleArgument
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.surfapi.core.api.generated.SoundKeys
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.transaction.api.TransactionApi
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.user.TransactionUser
import dev.slne.surf.transaction.velocity.commands.arguments.PlayerUuidArgumentType
import dev.slne.surf.transaction.velocity.commands.arguments.playerUuidArgument
import dev.slne.surf.transaction.velocity.plugin
import net.kyori.adventure.sound.Sound

private val log = logger()

fun payCommand() = commandAPICommand("pay") {
    withPermission("surf.transaction.pay")
    withAliases("bezahlen", "überweisen")

    playerUuidArgument("receiver")
    doubleArgument("amount", min = 1.0)

    playerExecutor { sender, args ->
        val (playerName, uuidDeferred) = args.getUnchecked<PlayerUuidArgumentType>("receiver")
            ?: error("Player UUID argument is missing")
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

            val senderUser = TransactionUser[sender.uniqueId]
            val receiverUser = TransactionUser[uuid]
            val currency = TransactionApi.defaultCurrency

            val (result) = senderUser.transfer(amount, currency, receiverUser)

            when (result) {
                TransactionResult.SUCCESS -> handleSuccess(sender, playerName, amount, currency)
                TransactionResult.RECEIVER_INSUFFICIENT_FUNDS -> handleReceiverInsufficientFunds(
                    sender,
                    playerName,
                    currency
                )

                TransactionResult.SENDER_INSUFFICIENT_FUNDS -> handleSenderInsufficientFunds(
                    sender,
                    currency
                )

                is TransactionResult.DATABASE_ERROR -> handleError(sender, result)
            }
        }
    }
}

private fun handleSuccess(
    sender: Player,
    receiver: String,
    amount: Double,
    currency: Currency
) {
    sender.sendText {
        appendPrefix()

        info("Du hast ")
        append(currency.format(amount))
        info(" an ")
        variableValue(receiver)
        info(" überwiesen.")
    }

    val receiverPlayer = plugin.proxy.getPlayer(receiver).orElse(null) ?: return

    receiverPlayer.sendText {
        appendPrefix()

        info("Du hast ")
        append(currency.format(amount))
        info(" von ")
        variableValue(sender.username)
        info(" erhalten.")
    }

    receiverPlayer.playSound {
        type(SoundKeys.ENTITY_CHICKEN_EGG)
        volume(.5f)
        source(Sound.Source.PLAYER)
    }
}

private fun handleError(sender: Player, error: TransactionResult.DATABASE_ERROR) {
    sender.sendText {
        appendPrefix()
        error("Es ist ein Fehler aufgetreten!")
    }

    log.atSevere()
        .withCause(error.cause)
        .log("Database error during transaction for player ${sender.username} (UUID: ${sender.uniqueId}): ${error.message ?: "No message provided"}")
}

private fun handleSenderInsufficientFunds(sender: Player, currency: Currency) {
    sender.sendText {
        appendPrefix()

        error("Du hast nicht genügend ")
        append(currency.displayName)
        error(" um diese Transaktion durchzuführen!")
    }
}

private fun handleReceiverInsufficientFunds(
    sender: Player,
    receiver: String,
    currency: Currency
) {
    sender.sendText {
        appendPrefix()

        error("Der Benutzer ")
        variableValue(receiver)
        error(" hat nicht genügend ")
        append(currency.displayName)
        error(" um diese Transaktion durchzuführen!")
    }
}