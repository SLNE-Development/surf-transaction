package dev.slne.surf.transaction.velocity.commands.pay

import com.github.shynixn.mccoroutine.velocity.launch
import com.velocitypowered.api.proxy.Player
import dev.jorel.commandapi.kotlindsl.commandAPICommand
import dev.jorel.commandapi.kotlindsl.doubleArgument
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.slne.surf.cloud.api.client.velocity.command.args.offlineCloudPlayerArgument
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.player.toOfflineCloudPlayer
import dev.slne.surf.surfapi.core.api.generated.SoundKeys
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.user.transfer
import dev.slne.surf.transaction.velocity.plugin
import kotlinx.coroutines.Deferred
import net.kyori.adventure.sound.Sound

private val log = logger()

fun payCommand() = commandAPICommand("pay") {
    withPermission("surf.transaction.pay")
    withAliases("bezahlen", "überweisen")

    offlineCloudPlayerArgument("receiver")
    doubleArgument("amount", min = 1.0)

    playerExecutor { sender, args ->
        val receiver: Deferred<OfflineCloudPlayer?> by args
        val amount: Double by args

        plugin.container.launch {
            val senderUser =
                sender.toOfflineCloudPlayer() ?: error("Sender is not a valid OfflineCloudPlayer")
            val receiverUser = receiver.await() ?: return@launch
            val currency = Currency.default()
            val result = senderUser.transfer(amount, currency, receiverUser)

            when (result) {
                TransactionResult.SUCCESS -> handleSuccess(sender, receiverUser, amount, currency)
                TransactionResult.RECEIVER_INSUFFICIENT_FUNDS -> handleReceiverInsufficientFunds(
                    sender,
                    receiverUser,
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

private suspend fun handleSuccess(
    sender: Player,
    receiver: OfflineCloudPlayer,
    amount: Double,
    currency: Currency
) {
    sender.sendText {
        appendPrefix()

        info("Du hast ")
        append(currency.format(amount))
        info(" an ")
        append(receiver.displayName())
        info(" überwiesen.")
    }

    val onlineReceiver = receiver.player ?: return

    onlineReceiver.sendText {
        appendPrefix()

        info("Du hast ")
        append(currency.format(amount))
        info(" von ")
        variableValue(sender.username)
        info(" erhalten.")
    }

    onlineReceiver.playSound {
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

private suspend fun handleReceiverInsufficientFunds(
    sender: Player,
    receiver: OfflineCloudPlayer,
    currency: Currency
) {
    sender.sendText {
        appendPrefix()

        error("Der Benutzer ")
        append(receiver.displayName())
        error(" hat nicht genügend ")
        append(currency.displayName)
        error(" um diese Transaktion durchzuführen!")
    }
}