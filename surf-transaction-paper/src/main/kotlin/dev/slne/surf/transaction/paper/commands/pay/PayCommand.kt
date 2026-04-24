package dev.slne.surf.transaction.paper.commands.pay

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.arguments.AsyncPlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.argument
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.doubleArgument
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.api.paper.command.util.awaitAsyncPlayerProfile
import dev.slne.surf.api.paper.command.util.idOrThrow
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.currency.CurrencyScale.Companion.maxValue
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.user.TransactionUser
import dev.slne.surf.transaction.api.user.transactionUser
import dev.slne.surf.transaction.core.client.redis.RedisService
import dev.slne.surf.transaction.core.common.component.Components
import dev.slne.surf.transaction.paper.commands.CommandPermission
import dev.slne.surf.transaction.paper.redis.events.pay.PaymentReceivedEvent
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import java.util.*

private val log = logger()

fun payCommand() = commandTree("pay") {
    withPermission(CommandPermission.PAY)
    withAliases("bezahlen", "überweisen")

    argument(AsyncPlayerProfileArgument("receiver")) {
        doubleArgument("amount", min = 1.0, max = Currency.default().scale.maxValue().toDouble()) {
            playerExecutorSuspend { sender, args ->
                pay(
                    sender,
                    args.awaitAsyncPlayerProfile("receiver").idOrThrow(),
                    args.getUnchecked("amount")!!
                )
            }
        }
    }
}

private suspend fun pay(
    sender: Player,
    receiverUuid: UUID,
    amount: Double
) {
    if (sender.uniqueId == receiverUuid) {
        throw CommandAPI.failWithString("Du kannst dir kein Geld selbst überweisen!")
    }

    sender.sendText {
        appendInfoPrefix()
        info("Überweisung wird ausgeführt...")
    }

    val currency = Currency.default()

    val receiverName = Components.usernameOrUuid(receiverUuid)
    val formattedAmount = currency.format(amount)
    val confirmed = payConfirmationDialog(sender, receiverName, formattedAmount)
    if (!confirmed) return

    val result = sender.transactionUser().transfer(
        amount = amount.toBigDecimal(),
        currency = currency,
        receiver = TransactionUser.byUuid(receiverUuid).getDefaultAccount()
    )

    when (result) {
        is TransactionResult.Success, is TransactionResult.TransferSuccess -> handleSuccess(
            sender,
            receiverUuid,
            receiverName,
            amount,
            currency,
            formattedAmount
        )

        TransactionResult.ReceiverInsufficientFunds -> handleReceiverInsufficientFunds(
            sender,
            receiverName,
            currency
        )

        TransactionResult.SenderInsufficientFunds -> handleSenderInsufficientFunds(
            sender,
            currency
        )

        is TransactionResult.DatabaseError -> handleError(sender, result)
    }
}

private suspend fun handleSuccess(
    sender: Player,
    receiver: UUID,
    receiverName: String,
    amount: Double,
    currency: Currency,
    amountComponent: Component
) {
    sender.showDialog(paySuccessDialog(receiverName, amountComponent))
    RedisService.publish(PaymentReceivedEvent(receiver, sender.name, currency, amount)).await()
}

private fun handleError(sender: Player, error: TransactionResult.DatabaseError) {
    sender.showDialog(payErrorDialog())

    log.atSevere()
        .withCause(error.cause.buildFakeThrowable())
        .log("Database error during transaction for player ${sender.name} (UUID: ${sender.uniqueId})")
}

private fun handleSenderInsufficientFunds(sender: Player, currency: Currency) {
    sender.showDialog(paySenderInsufficientFundsDialog(currency.displayName))
}

private fun handleReceiverInsufficientFunds(
    sender: Player,
    receiverName: String,
    currency: Currency
) {
    sender.showDialog(payReceiverInsufficientFundsDialog(currency.displayName, receiverName))
}