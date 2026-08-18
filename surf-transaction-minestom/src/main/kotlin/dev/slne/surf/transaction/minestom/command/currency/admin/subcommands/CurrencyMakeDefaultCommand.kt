package dev.slne.surf.transaction.minestom.command.currency.admin.subcommands

import dev.slne.minestom.lobby.api.command.commandapi.CommandAPI
import dev.slne.minestom.lobby.api.command.commandapi.argument.Argument
import dev.slne.minestom.lobby.api.command.commandapi.dsl.anyExecutorSuspend
import dev.slne.minestom.lobby.api.command.commandapi.dsl.literalArgument
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.core.client.command.TransactionPermissions
import dev.slne.surf.transaction.core.client.component.ClientComponents
import dev.slne.surf.transaction.core.client.currency.CurrencyServiceImpl
import dev.slne.surf.transaction.core.common.component.Components
import dev.slne.surf.transaction.core.common.currency.CurrencyImpl
import dev.slne.surf.transaction.minestom.command.arguments.currencyArgument
import net.minestom.server.command.CommandSender

fun Argument<String>.currencyMakeDefaultCommand() = literalArgument("makeDefault") {
    withPermission(TransactionPermissions.CURRENCY_ADMIN_MAKE_DEFAULT)

    currencyArgument("currencyName") {
        anyExecutorSuspend { sender, args ->
            makeDefault(sender, args.get("currencyName"))
        }
    }
}

private suspend fun makeDefault(sender: CommandSender, currency: Currency) {
    if (currency.defaultCurrency) {
        CommandAPI.failWithString(ClientComponents.CurrencyMessages.alreadyDefault(currency))
    }

    val result = CurrencyServiceImpl.INSTANCE.makeDefaultCurrency(currency as CurrencyImpl)

    sender.sendText {
        append(Components.Currency.formatChangedDefaultResult(currency, result))
    }
}
