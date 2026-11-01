package dev.slne.surf.transaction.paper.commands.arguments

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.core.client.component.ClientComponents
import org.bukkit.command.CommandSender

/**
 * Parses a [Currency] by name.
 *
 * Disabled currencies are suggested and accepted only for senders for whom [includeDisabled]
 * returns `true`; for everyone else parsing them fails with the "currency disabled" message.
 */
class CurrencyArgument(
    nodeName: String,
    includeDisabled: (CommandSender) -> Boolean
) : CustomArgument<Currency, String>(
    StringArgument(nodeName),
    { info ->
        val currency = Currency.byName(info.input())
            ?: throw CustomArgumentException.fromMessageBuilder(
                MessageBuilder()
                    .append("Currency ")
                    .appendArgInput()
                    .append(" not found")
            )

        if (!currency.enabled && !includeDisabled(info.sender())) {
            throw CustomArgumentException.fromAdventureComponent(
                ClientComponents.CurrencyMessages.disabled(currency)
            )
        }

        currency
    }
) {

    constructor(nodeName: String, includeDisabled: Boolean = false) :
            this(nodeName, { includeDisabled })

    init {
        replaceSuggestions(ArgumentSuggestions.stringCollection { info ->
            val withDisabled = includeDisabled(info.sender())

            Currency.all().filter { withDisabled || it.enabled }.map { it.name }
        })
    }
}

inline fun CommandTree.currencyArgument(
    nodeName: String,
    optional: Boolean = false,
    includeDisabled: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandTree =
    then(CurrencyArgument(nodeName, includeDisabled).setOptional(optional).apply(block))

inline fun Argument<*>.currencyArgument(
    nodeName: String,
    optional: Boolean = false,
    includeDisabled: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): Argument<*> =
    then(CurrencyArgument(nodeName, includeDisabled).setOptional(optional).apply(block))

inline fun CommandAPICommand.currencyArgument(
    nodeName: String,
    optional: Boolean = false,
    includeDisabled: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandAPICommand =
    withArguments(CurrencyArgument(nodeName, includeDisabled).setOptional(optional).apply(block))
