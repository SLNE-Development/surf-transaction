package dev.slne.surf.transaction.paper.commands.balance

import dev.jorel.commandapi.CommandAPIPaper
import dev.jorel.commandapi.arguments.AsyncPlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.argument
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.slne.surf.api.paper.command.executors.anyExecutorSuspend
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.api.paper.command.util.awaitAsyncPlayerProfile
import dev.slne.surf.api.paper.command.util.idOrThrow
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.currency.CurrencyDisabledException
import dev.slne.surf.transaction.api.user.TransactionUser
import dev.slne.surf.transaction.core.client.command.TransactionPermissions
import dev.slne.surf.transaction.core.client.component.ClientComponents
import dev.slne.surf.transaction.paper.commands.CommandPermission
import dev.slne.surf.transaction.paper.commands.arguments.CurrencyArgument
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

fun balanceCommand() = commandTree("balance") {
    withPermission(CommandPermission.BALANCE)
    withAliases("bal", "konto", "money")

    playerExecutorSuspend { sender, _ ->
        balance(
            sender,
            Currency.default(),
            sender.uniqueId,
            showCurrencyHint = true,
        )
    }

    argument(CurrencyArgument("currency", ::canUseDisabledCurrencies)) {
        playerExecutorSuspend { sender, args ->
            balance(
                sender,
                args.getUnchecked("currency")!!,
                sender.uniqueId
            )
        }

        argument(AsyncPlayerProfileArgument("player")) {
            withPermission(CommandPermission.BALANCE_OTHER)

            anyExecutorSuspend { sender, args ->
                balance(
                    sender,
                    args.getUnchecked("currency")!!,
                    args.awaitAsyncPlayerProfile("player").idOrThrow()
                )
            }
        }
    }
}

private suspend fun balance(
    sender: CommandSender,
    currency: Currency,
    queryTarget: UUID,
    showCurrencyHint: Boolean = false,
) {
    val isAdmin = canUseDisabledCurrencies(sender)
    val balance = try {
        TransactionUser.byUuid(queryTarget).balance(currency, ignoreDisabled = isAdmin)
    } catch (_: CurrencyDisabledException) {
        throw CommandAPIPaper.failWithAdventureComponent(
            ClientComponents.CurrencyMessages.disabled(currency)
        )
    }

    var message = if (sender is Player && queryTarget == sender.uniqueId) {
        ClientComponents.Balance.own(currency, balance, showCurrencyHint)
    } else {
        ClientComponents.Balance.other(currency, balance, queryTarget)
    }

    if (!currency.enabled) {
        message = message.append(ClientComponents.CurrencyMessages.disabledTag)
    }

    sender.sendMessage(message)
}

private fun canUseDisabledCurrencies(sender: CommandSender) =
    sender.hasPermission(TransactionPermissions.CURRENCY_ADMIN)
