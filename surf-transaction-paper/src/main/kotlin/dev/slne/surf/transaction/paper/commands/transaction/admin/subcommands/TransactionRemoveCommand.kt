package dev.slne.surf.transaction.paper.commands.transaction.admin.subcommands

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.doubleArgument
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.cloud.api.client.paper.command.args.offlineCloudPlayerArgument
import dev.slne.surf.cloud.api.common.player.CloudPlayer
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.surfapi.core.api.generated.SoundKeys
import dev.slne.surf.surfapi.core.api.messages.adventure.playSound
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.user.withdraw
import dev.slne.surf.transaction.paper.commands.CommandPermission
import dev.slne.surf.transaction.paper.commands.arguments.currencyArgument
import dev.slne.surf.transaction.paper.plugin
import kotlinx.coroutines.Deferred
import net.kyori.adventure.sound.Sound
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

private val log = logger()

fun Argument<*>.transactionRemoveCommand() = literalArgument("remove") {
    withPermission(CommandPermission.TRANSACTION_ADMIN_REMOVE)

    offlineCloudPlayerArgument("player") {
        currencyArgument("currency") {
            doubleArgument("amount", min = 1.0) {
                anyExecutor { sender, args ->
                    remove(
                        sender,
                        args.getUnchecked("player")!!,
                        args.getUnchecked("currency")!!,
                        args.getUnchecked("amount")!!
                    )
                }
            }
        }
    }
}

private fun remove(
    sender: CommandSender,
    player: Deferred<OfflineCloudPlayer?>,
    currency: Currency,
    amount: Double
) = plugin.launch {
    val user = player.await() ?: return@launch
    val result = user.withdraw(
        amount = amount,
        currency = currency,
        ignoreMinimum = true,
        additionalData = arrayOf(
            TransactionData(
                "admin.transaction.remove",
                (sender as? Player)?.uniqueId?.toString() ?: sender.name
            )
        )
    )

    if (result.success) {
        handleSuccess(sender, user.player, user.name() ?: "#UNKOWN", amount, currency)
    } else {
        handleError(sender, result, user.uuid)
    }
}


private fun handleError(sender: CommandSender, result: TransactionResult, receiverUuid: UUID) {
    sender.sendText {
        appendPrefix()
        error("Es ist ein Fehler aufgetreten!")
    }

    log.atSevere()
        .withCause((result as? TransactionResult.DATABASE_ERROR)?.cause)
        .log("An error occurred when trying to remove money from player with UUID $receiverUuid: ${result.message}")
}

private fun handleSuccess(
    sender: CommandSender,
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

            variableValue(sender.name)
            info(" hat dir ")
            append(currency.format(amount.toBigDecimal()))
            info(" abgezogen!")
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
        append(currency.format(amount.toBigDecimal()))
        success(" von ")
        variableValue(playerName)
        success(" abgezogen!")
    }
}