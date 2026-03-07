package dev.slne.surf.transaction.paper.server.commands.currency.admin.subcommands

import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.surfapi.bukkit.api.command.executors.anyExecutorSuspend
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.core.component.Components
import dev.slne.surf.transaction.core.currency.CurrencyImpl
import dev.slne.surf.transaction.core.currency.CurrencyServiceImpl
import dev.slne.surf.transaction.paper.server.commands.CommandPermission
import dev.slne.surf.transaction.paper.server.commands.arguments.currencyArgument
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
        appendPrefix()
        append(Components.Currency.formatChangedDefaultResult(currency, result))
    }
}