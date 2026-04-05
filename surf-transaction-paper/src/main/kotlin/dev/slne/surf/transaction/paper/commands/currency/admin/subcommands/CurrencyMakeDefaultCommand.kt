package dev.slne.surf.transaction.paper.commands.currency.admin.subcommands

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.command.executors.anyExecutorSuspend
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.core.common.component.Components
import dev.slne.surf.transaction.core.common.currency.CurrencyImpl
import dev.slne.surf.transaction.core.client.currency.CurrencyServiceImpl
import dev.slne.surf.transaction.paper.commands.CommandPermission
import dev.slne.surf.transaction.paper.commands.arguments.currencyArgument
import org.bukkit.command.CommandSender

fun Argument<*>.currencyMakeDefaultCommand() = literalArgument("makeDefault") {
    withPermission(CommandPermission.CURRENCY_ADMIN_MAKE_DEFAULT)

    currencyArgument("currencyName") {
        anyExecutorSuspend { sender, args ->
            makeDefault(sender, args.getUnchecked("currencyName")!!)
        }
    }
}

private suspend fun makeDefault(sender: CommandSender, currency: Currency) {
    if (currency.defaultCurrency) {
        throw CommandAPI.failWithString("Currency '${currency.name}' is already the default currency.")
    }

    val result = CurrencyServiceImpl.get().makeDefaultCurrency(currency as CurrencyImpl)

    sender.sendText {
        appendInfoPrefix()
        append(Components.Currency.formatChangedDefaultResult(currency, result))
    }
}