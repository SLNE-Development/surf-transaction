package dev.slne.surf.transaction.velocity.commands.arguments

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import com.velocitypowered.api.command.VelocityBrigadierMessage
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CommandAPIArgumentType
import dev.jorel.commandapi.executors.CommandArguments
import dev.slne.surf.surfapi.core.api.messages.adventure.text
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transactionApi

class CurrencyArgument(nodeName: String) :
    Argument<Currency>(nodeName, StringArgumentType.string()) {

    init {
        replaceSuggestions(ArgumentSuggestions.stringCollection { _ ->
            transactionApi.getCurrencies().map { it.name }
        })
    }

    override fun getPrimitiveType(): Class<Currency> {
        return Currency::class.java
    }

    override fun getArgumentType(): CommandAPIArgumentType {
        return CommandAPIArgumentType.PRIMITIVE_TEXT
    }

    override fun <Source : Any> parseArgument(
        cmdCtx: CommandContext<Source>,
        key: String,
        previousArgs: CommandArguments
    ): Currency {
        val currencyString = StringArgumentType.getString(cmdCtx, key)

        return transactionApi.getCurrencyByName(currencyString) ?: throw SimpleCommandExceptionType(
            VelocityBrigadierMessage.tooltip(text("Currency $currencyString not found"))
        ).create()
    }
}

inline fun CommandAPICommand.currencyArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandAPICommand = withArguments(
    CurrencyArgument(nodeName).setOptional(optional).apply(block)
)