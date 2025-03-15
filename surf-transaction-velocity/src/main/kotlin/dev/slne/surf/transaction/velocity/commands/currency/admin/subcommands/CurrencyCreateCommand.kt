package dev.slne.surf.transaction.velocity.commands.currency.admin.subcommands

import com.github.shynixn.mccoroutine.velocity.launch
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.*
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.transaction.api.currency.CurrencyScale
import dev.slne.surf.transaction.api.transactionApi
import dev.slne.surf.transaction.core.currency.*
import dev.slne.surf.transaction.velocity.plugin
import net.kyori.adventure.text.minimessage.MiniMessage

object CurrencyCreateCommand : CommandAPICommand("create") {
    init {
        withPermission("surf.transaction.currency.admin.create")

        // /currency create <currencyName> <symbol> <scale> <defaultCurrency> <displayName> <symbolDisplay>

        stringArgument("name")
        multiLiteralArgument("scale", CurrencyScale.entries.map { it.name.lowercase() })
        stringArgument("symbol")
        booleanArgument("defaultCurrency")
        stringArgument("displayName")
        stringArgument("symbolDisplay")

        anyExecutor { commandSource, args ->
            val name: String by args
            val scale: String by args
            val symbol: String by args
            val defaultCurrency: Boolean by args
            val displayName: String by args
            val symbolDisplay: String by args

            val currencyScale = CurrencyScale.valueOf(scale.uppercase())

            val miniMessage = MiniMessage.miniMessage()
            val currencyDisplayName = miniMessage.deserialize(displayName)
            val currencySymbolDisplay = miniMessage.deserialize(symbolDisplay)

            transactionApi.getCurrencyByName(name)?.let {
                commandSource.sendText {
                    error("Currency with name $name already exists")
                }

                return@anyExecutor
            }

            plugin.container.launch {
                val currency = CoreCurrency(
                    name = name,
                    scale = currencyScale,
                    displayName = currencyDisplayName,
                    symbol = symbol,
                    symbolDisplay = currencySymbolDisplay,
                    defaultCurrency = defaultCurrency,
                )

                val result = currencyService.createCurrency(currency)

                when (result) {
                    CurrencyCreateResult.SUCCESS -> {
                        commandSource.sendText {
                            success("Currency $name created successfully")
                        }
                    }

                    CurrencyCreateResult.ALREADY_EXISTS -> {
                        commandSource.sendText {
                            error("Currency with name $name already exists")
                        }
                    }

                    CurrencyCreateResult.DEFAULT_ALREADY_EXISTS -> {
                        commandSource.sendText {
                            error("Default currency already exists")
                        }
                    }

                    CurrencyCreateResult.INVALID_NAME -> {
                        commandSource.sendText {
                            error("Invalid currency name, must be between 1 and $CURRENCY_NAME_MAX_LENGTH characters")
                        }
                    }

                    CurrencyCreateResult.INVALID_SYMBOL -> {
                        commandSource.sendText {
                            error("Invalid currency symbol, must be between 1 and $CURRENCY_SYMBOL_MAX_LENGTH characters")
                        }
                    }
                }
            }
        }
    }
}