package dev.slne.surf.transaction.minestom.command.arguments

import dev.slne.minestom.lobby.api.command.commandapi.CommandAPICommand
import dev.slne.minestom.lobby.api.command.commandapi.CommandTree
import dev.slne.minestom.lobby.api.command.commandapi.argument.Argument
import dev.slne.minestom.lobby.api.command.commandapi.argument.MultiLiteralArgument
import dev.slne.minestom.lobby.api.command.commandapi.executor.CommandArguments
import dev.slne.surf.transaction.api.currency.CurrencyScale
import dev.slne.surf.transaction.core.client.currency.CurrencyScaleNames

fun currencyScaleArgument0(nodeName: String) = MultiLiteralArgument(
    nodeName,
    *CurrencyScaleNames.all.toTypedArray()
)

inline fun CommandTree.currencyScaleArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<String>.() -> Unit = {}
): CommandTree = then(currencyScaleArgument0(nodeName).setOptional(optional).apply(block))

inline fun <T> Argument<T>.currencyScaleArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<String>.() -> Unit = {}
): Argument<T> = then(currencyScaleArgument0(nodeName).setOptional(optional).apply(block))

inline fun CommandAPICommand.currencyScaleArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<String>.() -> Unit = {}
): CommandAPICommand =
    withArguments(currencyScaleArgument0(nodeName).setOptional(optional).apply(block))

fun CommandArguments.getCurrencyScale(nodeName: String): CurrencyScale =
    CurrencyScaleNames.parse(get<String>(nodeName))
