package dev.slne.surf.transaction.paper.commands.arguments

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.slne.surf.transaction.api.currency.Currency

class CurrencyArgument(nodeName: String) : CustomArgument<Currency, String>(
    StringArgument(nodeName),
    { info ->
        Currency.byName(info.input()) ?: throw CustomArgumentException.fromMessageBuilder(
            MessageBuilder()
                .append("Currency ")
                .appendArgInput()
                .append(" not found")
        )
    }
) {

    init {
        replaceSuggestions(ArgumentSuggestions.stringCollection { _ ->
            Currency.all().map { it.name }
        })
    }
}

inline fun CommandTree.currencyArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandTree = then(CurrencyArgument(nodeName).setOptional(optional).apply(block))

inline fun Argument<*>.currencyArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): Argument<*> = then(CurrencyArgument(nodeName).setOptional(optional).apply(block))

inline fun CommandAPICommand.currencyArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandAPICommand =
    withArguments(CurrencyArgument(nodeName).setOptional(optional).apply(block))