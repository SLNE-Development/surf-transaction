package dev.slne.surf.transaction.paper.commands.transaction.admin.subcommands

import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.AsyncPlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.argument
import dev.jorel.commandapi.kotlindsl.doubleArgument
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.surfapi.bukkit.api.command.executors.anyExecutorSuspend
import dev.slne.surf.surfapi.bukkit.api.command.util.awaitAsyncPlayerProfile
import dev.slne.surf.surfapi.bukkit.api.command.util.idOrThrow
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.user.TransactionUser
import dev.slne.surf.transaction.core.common.component.Components
import dev.slne.surf.transaction.core.client.redis.RedisService
import dev.slne.surf.transaction.paper.commands.CommandPermission
import dev.slne.surf.transaction.paper.commands.arguments.currencyArgument
import dev.slne.surf.transaction.paper.redis.events.transaction.AdminTransactionEvent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

private val log = logger()

fun Argument<*>.transactionAddCommand() = literalArgument("add") {
    withPermission(CommandPermission.TRANSACTION_ADMIN_ADD)

    argument(AsyncPlayerProfileArgument("player")) {
        currencyArgument("currency") {
            doubleArgument("amount", min = 1.0) {
                anyExecutorSuspend { sender, args ->
                    add(
                        sender,
                        args.awaitAsyncPlayerProfile("player").idOrThrow(),
                        args.getUnchecked("currency")!!,
                        args.getUnchecked("amount")!!
                    )
                }
            }
        }
    }
}

private suspend fun add(
    sender: CommandSender,
    playerUuid: UUID,
    currency: Currency,
    amount: Double
) {
    val result = TransactionUser.byUuid(playerUuid).deposit(
        amount = amount.toBigDecimal(),
        currency = currency,
        additionalData = arrayOf(
            TransactionData.of(
                "admin.transaction.add",
                (sender as? Player)?.uniqueId?.toString() ?: sender.name
            )
        )
    )

    if (result.success) {
        handleSuccess(sender, playerUuid, amount, currency)
    } else {
        handleError(sender, result, playerUuid)

    }
}

private fun handleError(sender: CommandSender, result: TransactionResult, receiverUuid: UUID) {
    sender.sendText {
        appendErrorPrefix()
        error("Es ist ein Fehler aufgetreten!")
    }

    log.atSevere()
        .withCause((result as? TransactionResult.DatabaseError)?.cause?.buildFakeThrowable())
        .log("An error occurred when trying to add money to player with UUID $receiverUuid")
}

private suspend fun handleSuccess(
    sender: CommandSender,
    receiverUuid: UUID,
    amount: Double,
    currency: Currency
) {
    sender.sendText {
        appendSuccessPrefix()

        darkSpacer("[")
        variableKey("Admin")
        darkSpacer("] ")

        success("Du hast ")
        append(currency.format(amount))
        success(" an ")
        variableValue(Components.usernameOrUuid(receiverUuid))
        success(" gesendet!")
    }

    val event = AdminTransactionEvent(
        receiverUuid,
        currency,
        amount,
        sender.name,
        added = true
    )
    RedisService.publish(event).await()
}