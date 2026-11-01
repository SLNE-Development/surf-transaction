package dev.slne.surf.transaction.paper.commands.currency.admin.subcommands

import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.command.executors.anyExecutorSuspend
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.core.client.currency.CurrencyServiceImpl
import dev.slne.surf.transaction.core.common.component.Components
import dev.slne.surf.transaction.core.common.currency.CurrencyImpl
import dev.slne.surf.transaction.paper.commands.CommandPermission
import dev.slne.surf.transaction.paper.commands.arguments.currencyArgument
import org.bukkit.command.CommandSender

fun Argument<*>.currencyEnableCommand() = literalArgument("enable") {
    withPermission(CommandPermission.CURRENCY_ADMIN_ENABLE)

    currencyArgument("currencyName", includeDisabled = true) {
        anyExecutorSuspend { sender, args ->
            setEnabled(sender, args.getUnchecked("currencyName")!!, true)
        }
    }
}

fun Argument<*>.currencyDisableCommand() = literalArgument("disable") {
    withPermission(CommandPermission.CURRENCY_ADMIN_DISABLE)

    currencyArgument("currencyName", includeDisabled = true) {
        anyExecutorSuspend { sender, args ->
            setEnabled(sender, args.getUnchecked("currencyName")!!, false)
        }
    }
}

private suspend fun setEnabled(sender: CommandSender, currency: Currency, enabled: Boolean) {
    val result = CurrencyServiceImpl.INSTANCE.setCurrencyEnabled(currency as CurrencyImpl, enabled)

    sender.sendText {
        append(Components.Currency.formatCurrencyEnabledResult(currency, enabled, result))
    }
}
