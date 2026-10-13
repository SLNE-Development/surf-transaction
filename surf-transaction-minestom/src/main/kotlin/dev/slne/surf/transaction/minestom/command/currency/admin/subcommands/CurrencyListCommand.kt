package dev.slne.surf.transaction.minestom.command.currency.admin.subcommands

import dev.slne.surf.api.minestom.command.argument.Argument
import dev.slne.surf.api.minestom.command.dsl.anyExecutor
import dev.slne.surf.api.minestom.command.dsl.literalArgument
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.core.client.command.TransactionPermissions
import dev.slne.surf.transaction.core.client.component.ClientComponents

fun Argument<String>.listCurrenciesCommand() = literalArgument("list") {
    withPermission(TransactionPermissions.CURRENCY_ADMIN_LIST)

    anyExecutor { sender, _ ->
        sender.sendMessage(
            ClientComponents.CurrencyMessages.pagination.renderComponent(Currency.all())
        )
    }
}
