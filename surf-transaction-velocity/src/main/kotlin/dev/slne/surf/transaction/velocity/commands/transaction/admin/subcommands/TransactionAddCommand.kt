package dev.slne.surf.transaction.velocity.commands.transaction.admin.subcommands

import com.github.shynixn.mccoroutine.velocity.launch
import com.velocitypowered.api.proxy.Player
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.doubleArgument
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.cloud.api.client.velocity.command.args.offlineCloudPlayerArgument
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.surfapi.core.api.generated.SoundKeys
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.user.deposit
import dev.slne.surf.transaction.velocity.commands.arguments.currencyArgument
import dev.slne.surf.transaction.velocity.plugin
import kotlinx.coroutines.Deferred
import net.kyori.adventure.sound.Sound
import java.util.*

private val log = logger()

fun CommandAPICommand.transactionAddCommand() = subcommand("add") {
    withPermission("surf.transaction.admin.add")

    offlineCloudPlayerArgument("player")
    currencyArgument("currency")
    doubleArgument("amount", min = 1.0)

    playerExecutor { sender, args ->
        val player: Deferred<OfflineCloudPlayer?> by args
        val currency: Currency by args
        val amount: Double by args

        plugin.container.launch {
            val user = player.await() ?: return@launch
            val result = user.deposit(
                amount,
                currency,
                TransactionData("admin.transaction.add", sender.uniqueId.toString())
            )

            if (result == TransactionResult.SUCCESS) {
                handleSuccess(sender, user.player, user.name() ?: "#UNKOWN", amount, currency)
            } else {
                handleError(sender, result, user.uuid)
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
    receiver: CloudPlayer?,
    playerName: String,
    amount: Double,
    currency: Currency
) {
    if (receiver != null) {
        receiver.sendText {
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

        receiver.playSound {
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