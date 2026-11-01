package dev.slne.surf.transaction.minestom.command.balance

import dev.slne.surf.api.core.messages.adventure.hasPermission
import dev.slne.surf.api.minestom.command.dsl.anyExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.commandTree
import dev.slne.surf.api.minestom.command.dsl.playerExecutorSuspend
import dev.slne.surf.core.api.minestom.command.argument.surfOfflinePlayerArgument
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.currency.CurrencyDisabledException
import dev.slne.surf.transaction.api.user.TransactionUser
import dev.slne.surf.transaction.core.client.command.TransactionPermissions
import dev.slne.surf.transaction.core.client.component.ClientComponents
import dev.slne.surf.transaction.minestom.command.arguments.CurrencyArgument
import dev.slne.surf.transaction.minestom.command.awaitSurfOfflinePlayerUuid
import net.minestom.server.command.CommandSender
import net.minestom.server.entity.Player
import java.util.UUID

fun balanceCommand() = commandTree("balance") {
    withPermission(TransactionPermissions.BALANCE)
    withAliases("bal", "konto", "money")

    playerExecutorSuspend { sender, _ ->
        balance(
            sender,
            Currency.default(),
            sender.uuid,
            showCurrencyHint = true,
        )
    }

    val currencyArgument = CurrencyArgument("currency") {
        it.hasPermission(TransactionPermissions.CURRENCY_ADMIN)
    }

    then(currencyArgument.apply {
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
    })
}

private suspend fun balance(
    sender: CommandSender,
    currency: Currency,
    queryTarget: UUID,
    showCurrencyHint: Boolean = false,
) {
    val canUseDisabled = sender.hasPermission(TransactionPermissions.CURRENCY_ADMIN)
    val balance = try {
        TransactionUser.byUuid(queryTarget).balance(currency, ignoreDisabled = canUseDisabled)
    } catch (_: CurrencyDisabledException) {
        sender.sendMessage(ClientComponents.CurrencyMessages.disabled(currency))
        return
    }

    val message = if (sender is Player && queryTarget == sender.uuid) {
        ClientComponents.Balance.own(currency, balance, showCurrencyHint)
    } else {
        ClientComponents.Balance.other(currency, balance, queryTarget)
    }

    sender.sendMessage(
        if (currency.enabled) {
            message
        } else {
            message.append(ClientComponents.CurrencyMessages.disabledTag)
        }
    )
}
