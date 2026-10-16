package dev.slne.surf.transaction.minestom.command.arguments

import dev.slne.surf.api.minestom.command.CommandAPI
import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.CommandTree
import dev.slne.surf.api.minestom.command.argument.Argument
import dev.slne.surf.api.minestom.command.argument.CustomArgument
import dev.slne.surf.api.minestom.command.argument.StringArgument
import dev.slne.surf.api.minestom.command.suggestion.ArgumentSuggestions
import dev.slne.surf.transaction.api.currency.Currency

class CurrencyArgument(nodeName: String) : CustomArgument<Currency, String>(
    StringArgument(nodeName),
    { info ->
        Currency.byName(info.currentInput)
            ?: CommandAPI.failWithString("Currency ${info.currentInput} not found")
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
    block: Argument<Currency>.() -> Unit = {}
): CommandTree = then(CurrencyArgument(nodeName).setOptional(optional).apply(block))

inline fun <T> Argument<T>.currencyArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<Currency>.() -> Unit = {}
): Argument<T> = then(CurrencyArgument(nodeName).setOptional(optional).apply(block))

inline fun CommandAPICommand.currencyArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<Currency>.() -> Unit = {}
): CommandAPICommand =
    withArguments(CurrencyArgument(nodeName).setOptional(optional).apply(block))
