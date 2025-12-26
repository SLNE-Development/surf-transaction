package dev.slne.surf.transaction.paper.commands.pay

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.arguments.AsyncPlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.argument
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.jorel.commandapi.kotlindsl.doubleArgument
import dev.slne.surf.surfapi.bukkit.api.command.executors.playerExecutorSuspend
import dev.slne.surf.surfapi.bukkit.api.command.util.awaitAsyncPlayerProfile
import dev.slne.surf.surfapi.bukkit.api.command.util.idOrThrow
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.user.TransactionUser
import dev.slne.surf.transaction.api.user.transactionUser
import dev.slne.surf.transaction.core.component.Components
import dev.slne.surf.transaction.core.redis.RedisService
import dev.slne.surf.transaction.paper.commands.CommandPermission
import dev.slne.surf.transaction.paper.redis.events.pay.PaymentReceivedEvent
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

private val log = logger()
private val payLock = ConcurrentHashMap.newKeySet<UUID>()

fun payCommand() = commandTree("pay") {
    withPermission(CommandPermission.PAY)
    withAliases("bezahlen", "überweisen")

    argument(AsyncPlayerProfileArgument("receiver")) {
        doubleArgument("amount", min = 1.0) {
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

    if (!payLock.add(sender.uniqueId)) {
        throw CommandAPI.failWithString("Du führst bereits eine Überweisung durch. Bitte warte einen Moment...")
    }

    sender.sendText {
        appendPrefix()
        info("Überweisung wird ausgeführt...")
    }

    try {
        val currency = Currency.default()
        val result = sender.transactionUser().transfer(
            amount = amount.toBigDecimal(),
            currency = currency,
            receiver = TransactionUser.byUuid(receiverUuid).getDefaultAccount()
        )

        when (result) {
            is TransactionResult.Success, is TransactionResult.TransferSuccess -> handleSuccess(
                sender,
                receiverUuid,
                amount,
                currency
            )

            TransactionResult.ReceiverInsufficientFunds -> handleReceiverInsufficientFunds(
                sender,
                receiverUuid,
                currency
            )

            TransactionResult.SenderInsufficientFunds -> handleSenderInsufficientFunds(
                sender,
                currency
            )

            is TransactionResult.DatabaseError -> handleError(sender, result)
        }
    } finally {
        payLock.remove(sender.uniqueId)
    }
}

private suspend fun handleSuccess(
    sender: Player,
    receiver: UUID,
    amount: Double,
    currency: Currency
) {
    sender.sendText {
        appendPrefix()

        info("Du hast ")
        append(currency.format(amount))
        info(" an ")
        append(Components.usernameOrUuidComponent(receiver))
        info(" überwiesen.")
    }

    RedisService.publish(PaymentReceivedEvent(receiver, sender.name, currency, amount)).await()
}

private fun handleError(sender: Player, error: TransactionResult.DatabaseError) {
    sender.sendText {
        appendPrefix()
        error("Es ist ein Fehler aufgetreten!")
    }

    log.atSevere()
        .withCause(error.cause)
        .log("Database error during transaction for player ${sender.name} (UUID: ${sender.uniqueId})")
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
    receiver: UUID,
    currency: Currency
) {
    sender.sendText {
        appendPrefix()

        append(Components.usernameOrUuidComponent(receiver))
        error(" hat nicht genügend ")
        append(currency.displayName)
        error(" um diese Transaktion durchzuführen!")
    }
}