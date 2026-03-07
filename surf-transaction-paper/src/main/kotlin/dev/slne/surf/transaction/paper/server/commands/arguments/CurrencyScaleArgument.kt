package dev.slne.surf.transaction.paper.server.commands.arguments

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.MultiLiteralArgument
import dev.jorel.commandapi.executors.CommandArguments
import dev.slne.surf.transaction.api.currency.CurrencyScale

fun currencyScaleArgument0(nodeName: String) = MultiLiteralArgument(
    nodeName,
    *CurrencyScale.entries.map { it.name.lowercase() }.toTypedArray()
)

inline fun CommandTree.currencyScaleArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandTree = then(currencyScaleArgument0(nodeName).setOptional(optional).apply(block))

inline fun Argument<*>.currencyScaleArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): Argument<*> = then(currencyScaleArgument0(nodeName).setOptional(optional).apply(block))

inline fun CommandAPICommand.currencyScaleArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandAPICommand =
    withArguments(currencyScaleArgument0(nodeName).setOptional(optional).apply(block))

fun CommandArguments.getCurrencyScale(nodeName: String): CurrencyScale =
    CurrencyScale.valueOf(getUnchecked<String>(nodeName)!!.uppercase())