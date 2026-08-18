package dev.slne.surf.transaction.minestom.command.transaction.admin.subcommands

import dev.slne.minestom.lobby.api.command.commandapi.argument.Argument
import dev.slne.minestom.lobby.api.command.commandapi.dsl.anyExecutorSuspend
import dev.slne.minestom.lobby.api.command.commandapi.dsl.doubleArgument
import dev.slne.minestom.lobby.api.command.commandapi.dsl.literalArgument
import dev.slne.surf.api.core.util.logger
import dev.slne.surf.core.api.minestom.command.argument.surfOfflinePlayerArgument
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.user.TransactionUser
import dev.slne.surf.transaction.core.client.command.TransactionPermissions
import dev.slne.surf.transaction.core.client.component.ClientComponents
import dev.slne.surf.transaction.core.client.redis.RedisService
import dev.slne.surf.transaction.minestom.command.arguments.currencyArgument
import dev.slne.surf.transaction.minestom.command.awaitSurfOfflinePlayerUuid
import dev.slne.surf.transaction.minestom.command.senderId
import dev.slne.surf.transaction.minestom.command.senderName
import dev.slne.surf.transaction.paper.redis.events.transaction.AdminTransactionEvent
import net.minestom.server.command.CommandSender
import java.util.UUID

private val log = logger()

fun Argument<String>.transactionAddCommand() = literalArgument("add") {
    withPermission(TransactionPermissions.TRANSACTION_ADMIN_ADD)

    surfOfflinePlayerArgument("player") {
        currencyArgument("currency") {
            doubleArgument("amount", min = 1.0) {
                anyExecutorSuspend { sender, args ->
                    add(
                        sender,
                        args.awaitSurfOfflinePlayerUuid("player"),
                        args.get("currency"),
                        args.get("amount")
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
            TransactionData.of("admin.transaction.add", sender.senderId)
        )
    )

    if (result.success) {
        handleSuccess(sender, playerUuid, amount, currency)
    } else {
        handleError(sender, result, playerUuid)
    }
}

private fun handleError(sender: CommandSender, result: TransactionResult, receiverUuid: UUID) {
    sender.sendMessage(ClientComponents.Transaction.adminFailure())

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
    sender.sendMessage(
        ClientComponents.Transaction.adminAdded(currency, amount, receiverUuid)
    )

    val event = AdminTransactionEvent(
        receiverUuid,
        currency,
        amount,
        sender.senderName,
        added = true
    )
    RedisService.publish(event).await()
}
