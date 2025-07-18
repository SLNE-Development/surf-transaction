package dev.slne.surf.transaction.velocity.commands.currency.admin.subcommands

import com.github.shynixn.mccoroutine.velocity.launch
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.stringArgument
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.transaction.api.TransactionApi
import dev.slne.surf.transaction.core.currency.*
import dev.slne.surf.transaction.velocity.plugin

fun currencyMakeDefaultCommand() = subcommand("makeDefault") {
    withPermission("surf.transaction.currency.admin.makeDefault")

    stringArgument("currencyName")

    anyExecutor { sender, args ->
        val currencyName: String by args
        val currency = TransactionApi.getCurrencyByName(currencyName)
        if (currency == null) {
            throw CommandAPI.failWithString("Currency with name '$currencyName' does not exist.")
        }
        if (currency.defaultCurrency) {
            throw CommandAPI.failWithString("Currency '$currencyName' is already the default currency.")
        }

        plugin.container.launch {
            val result = CurrencyService.makeDefaultCurrency(currency as CoreCurrency)

            when (result) {
                CurrencyCreateResult.SUCCESS -> {
                    sender.sendText {
                        success("Currency $name has been set as the default currency")
                    }
                }

                CurrencyCreateResult.ALREADY_EXISTS -> {
                    sender.sendText {
                        error("Currency with name $name already exists")
                    }
                }

                CurrencyCreateResult.DEFAULT_ALREADY_EXISTS -> {
                    sender.sendText {
                        error("Default currency already exists")
                    }
                }

                CurrencyCreateResult.INVALID_NAME -> {
                    sender.sendText {
                        error("Invalid currency name, must be between 1 and $CURRENCY_NAME_MAX_LENGTH characters")
                    }
                }

                CurrencyCreateResult.INVALID_SYMBOL -> {
                    sender.sendText {
                        error("Invalid currency symbol, must be between 1 and $CURRENCY_SYMBOL_MAX_LENGTH characters")
                    }
                }
            }
        }
    }
}