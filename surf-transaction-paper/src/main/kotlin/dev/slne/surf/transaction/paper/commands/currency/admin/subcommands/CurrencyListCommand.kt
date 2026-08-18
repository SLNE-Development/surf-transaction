package dev.slne.surf.transaction.paper.commands.currency.admin.subcommands

import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.core.client.component.ClientComponents
import dev.slne.surf.transaction.paper.commands.CommandPermission

fun Argument<*>.listCurrenciesCommand() = literalArgument("list") {
    withPermission(CommandPermission.CURRENCY_ADMIN_LIST)

    anyExecutor { sender, _ ->
        sender.sendMessage(
            ClientComponents.CurrencyMessages.pagination.renderComponent(Currency.all())
        )
    }
}
