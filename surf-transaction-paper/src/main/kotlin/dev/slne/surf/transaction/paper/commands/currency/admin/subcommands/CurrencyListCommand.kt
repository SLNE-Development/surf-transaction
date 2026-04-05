package dev.slne.surf.transaction.paper.commands.currency.admin.subcommands

import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.api.core.messages.CommonComponents
import dev.slne.surf.api.core.messages.adventure.buildText
import dev.slne.surf.api.core.messages.pagination.Pagination
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.paper.commands.CommandPermission

private val pagination = Pagination<Currency> {
    title { primary("Currencies") }
    rowRenderer { currency, _ ->
        listOf(
            buildText {
                append(CommonComponents.EM_DASH)
                appendSpace()
                append(currency)
                appendSpace()
                append(CommonComponents.EM_DASH)
                variableKey(" Default: ")
                variableValue(currency.defaultCurrency)
            }
        )
    }
}

fun Argument<*>.listCurrenciesCommand() = literalArgument("list") {
    withPermission(CommandPermission.CURRENCY_ADMIN_LIST)

    anyExecutor { sender, _ ->
        sender.sendMessage(pagination.renderComponent(Currency.all()))
    }
}