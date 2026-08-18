package dev.slne.surf.transaction.minestom.command.balance

import dev.slne.minestom.lobby.api.command.commandapi.dsl.anyExecutorSuspend
import dev.slne.minestom.lobby.api.command.commandapi.dsl.commandTree
import dev.slne.minestom.lobby.api.command.commandapi.dsl.playerExecutorSuspend
import dev.slne.surf.core.api.minestom.command.argument.surfOfflinePlayerArgument
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.user.TransactionUser
import dev.slne.surf.transaction.core.client.command.TransactionPermissions
import dev.slne.surf.transaction.core.client.component.ClientComponents
import dev.slne.surf.transaction.minestom.command.arguments.currencyArgument
import dev.slne.surf.transaction.minestom.command.awaitSurfOfflinePlayerUuid
import net.minestom.server.command.CommandSender
import net.minestom.server.entity.Player
import java.util.UUID

fun balanceCommand() = commandTree("balance") {
    withPermission(TransactionPermissions.BALANCE)
    withAliases("bal", "konto", "money")

    currencyArgument("currency") {
        playerExecutorSuspend { sender, args ->
            balance(
                sender,
                args.get("currency"),
                sender.uuid
            )
        }

        surfOfflinePlayerArgument("player") {
            withPermission(TransactionPermissions.BALANCE_OTHER)

            anyExecutorSuspend { sender, args ->
                balance(
                    sender,
                    args.get("currency"),
                    args.awaitSurfOfflinePlayerUuid("player")
                )
            }
        }
    }
}

private suspend fun balance(
    sender: CommandSender,
    currency: Currency,
    queryTarget: UUID
) {
    val balance = TransactionUser.byUuid(queryTarget).balance(currency)

    sender.sendMessage(
        if (sender is Player && queryTarget == sender.uuid) {
            ClientComponents.Balance.own(currency, balance)
        } else {
            ClientComponents.Balance.other(currency, balance, queryTarget)
        }
    )
}
