package dev.slne.surf.transaction.minestom.command.pay

import dev.slne.minestom.lobby.api.command.commandapi.CommandAPI
import dev.slne.minestom.lobby.api.command.commandapi.dsl.commandTree
import dev.slne.minestom.lobby.api.command.commandapi.dsl.doubleArgument
import dev.slne.minestom.lobby.api.command.commandapi.dsl.playerExecutorSuspend
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.core.api.minestom.command.argument.surfOfflinePlayerArgument
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.user.TransactionUser
import dev.slne.surf.transaction.api.user.transactionUser
import dev.slne.surf.transaction.core.client.command.PayAmount
import dev.slne.surf.transaction.core.client.command.TransactionPermissions
import dev.slne.surf.transaction.core.client.component.ClientComponents
import dev.slne.surf.transaction.core.client.redis.RedisService
import dev.slne.surf.transaction.core.common.component.Components
import dev.slne.surf.transaction.minestom.command.awaitSurfOfflinePlayerUuid
import dev.slne.surf.transaction.paper.redis.events.pay.PaymentReceivedEvent
import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player
import java.util.UUID

private val log = logger()

fun payCommand() = commandTree("pay") {
    withPermission(TransactionPermissions.PAY)
    withAliases("bezahlen", "überweisen")

    surfOfflinePlayerArgument("receiver") {
        doubleArgument("amount", min = PayAmount.MINIMUM, max = PayAmount.MAXIMUM) {
            playerExecutorSuspend { sender, args ->
                pay(
                    sender,
                    args.awaitSurfOfflinePlayerUuid("receiver"),
                    args.get("amount")
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
    if (sender.uuid == receiverUuid) {
        CommandAPI.failWithString(ClientComponents.Pay.SELF_TRANSFER_NOT_ALLOWED)
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
    RedisService.publish(PaymentReceivedEvent(receiver, sender.username, currency, amount)).await()
}

private fun handleError(sender: Player, error: TransactionResult.DatabaseError) {
    sender.showDialog(payErrorDialog())

    log.atSevere()
        .withCause(error.cause.buildFakeThrowable())
        .log("Database error during transaction for player ${sender.username} (UUID: ${sender.uuid})")
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
