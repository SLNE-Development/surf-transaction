package dev.slne.surf.transaction.minestom.command.currency.admin.subcommands

import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.minestom.command.CommandAPI
import dev.slne.surf.api.minestom.command.argument.Argument
import dev.slne.surf.api.minestom.command.dsl.anyExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.literalArgument
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.core.client.command.TransactionPermissions
import dev.slne.surf.transaction.core.client.currency.CurrencyServiceImpl
import dev.slne.surf.transaction.core.common.component.Components
import dev.slne.surf.transaction.core.common.currency.CurrencyImpl
import dev.slne.surf.transaction.minestom.command.arguments.currencyArgument
import net.minestom.server.command.CommandSender

fun Argument<String>.currencyDisableCommand() = literalArgument("disable") {
    withPermission(TransactionPermissions.CURRENCY_ADMIN_DISABLE)

    currencyArgument("currencyName", includeDisabled = true) {
        anyExecutorSuspend { sender, args ->
            disable(sender, args.get("currencyName"))
        }
    }
}

private suspend fun disable(sender: CommandSender, currency: Currency) {
    val result = CurrencyServiceImpl.INSTANCE.setCurrencyEnabled(currency as CurrencyImpl, false)

    sender.sendText {
        append(Components.Currency.formatCurrencyEnabledResult(currency, false, result))
    }
}
