package dev.slne.surf.transaction.minestom.command.arguments

import dev.slne.surf.api.minestom.command.CommandAPI
import dev.slne.surf.api.minestom.command.CommandAPICommand
import dev.slne.surf.api.minestom.command.CommandTree
import dev.slne.surf.api.minestom.command.argument.Argument
import dev.slne.surf.api.minestom.command.argument.CustomArgument
import dev.slne.surf.api.minestom.command.argument.StringArgument
import dev.slne.surf.api.minestom.command.suggestion.ArgumentSuggestions
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.core.client.component.ClientComponents
import net.minestom.server.command.CommandSender

/**
 * Argument that parses and suggests [Currency] names.
 *
 * Disabled currencies are only suggested and accepted for senders for whom [includeDisabled]
 * returns `true`; for everyone else parsing a disabled currency fails with the disabled message.
 */
class CurrencyArgument(
    nodeName: String,
    includeDisabled: (CommandSender) -> Boolean,
) : CustomArgument<Currency, String>(
    StringArgument(nodeName),
    { info ->
        val currency = Currency.byName(info.currentInput)
            ?: CommandAPI.failWithString("Currency ${info.currentInput} not found")

        if (!currency.enabled && !includeDisabled(info.sender)) {
            CommandAPI.failWithMessage(ClientComponents.CurrencyMessages.disabled(currency))
        }

        currency
    }
) {

    constructor(nodeName: String, includeDisabled: Boolean = false) :
            this(nodeName, { includeDisabled })

    init {
        replaceSuggestions(ArgumentSuggestions.stringCollection { info ->
            val sender = info.sender
            val currencies = if (includeDisabled(sender)) Currency.all() else Currency.allEnabled()

            currencies.map { it.name }
        })
    }
}

inline fun CommandTree.currencyArgument(
    nodeName: String,
    optional: Boolean = false,
    includeDisabled: Boolean = false,
    block: Argument<Currency>.() -> Unit = {}
): CommandTree =
    then(CurrencyArgument(nodeName, includeDisabled).setOptional(optional).apply(block))

inline fun <T> Argument<T>.currencyArgument(
    nodeName: String,
    optional: Boolean = false,
    includeDisabled: Boolean = false,
    block: Argument<Currency>.() -> Unit = {}
): Argument<T> =
    then(CurrencyArgument(nodeName, includeDisabled).setOptional(optional).apply(block))

inline fun CommandAPICommand.currencyArgument(
    nodeName: String,
    optional: Boolean = false,
    includeDisabled: Boolean = false,
    block: Argument<Currency>.() -> Unit = {}
): CommandAPICommand =
    withArguments(CurrencyArgument(nodeName, includeDisabled).setOptional(optional).apply(block))
