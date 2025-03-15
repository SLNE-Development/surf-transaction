package dev.slne.surf.transaction.velocity.commands.currency.admin.subcommands

import com.github.shynixn.mccoroutine.velocity.launch
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.*
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.velocity.api.command.args.miniMessageArgument
import dev.slne.surf.transaction.api.currency.CurrencyScale
import dev.slne.surf.transaction.api.transactionApi
import dev.slne.surf.transaction.core.currency.*
import dev.slne.surf.transaction.velocity.plugin
import net.kyori.adventure.text.Component
import java.math.BigDecimal

object CurrencyCreateCommand : CommandAPICommand("create") {
    init {
        withPermission("surf.transaction.currency.admin.create")

        stringArgument("name")
        multiLiteralArgument("scale", CurrencyScale.entries.map { it.name.lowercase() })
        stringArgument("symbol")
        booleanArgument("defaultCurrency")
        doubleArgument("minimumAmount")
        miniMessageArgument("displayName")
        miniMessageArgument("symbolDisplay")

        anyExecutor { commandSource, args ->
            val name: String by args
            val scale: String by args
            val symbol: String by args
            val defaultCurrency: Boolean by args
            val minimumAmount: Double by args
            val displayName: Component by args
            val symbolDisplay: Component by args

            val currencyScale = CurrencyScale.valueOf(scale.uppercase())

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
                    displayName = displayName,
                    symbol = symbol,
                    symbolDisplay = symbolDisplay,
                    defaultCurrency = defaultCurrency,
                    minimumAmount = BigDecimal.valueOf(minimumAmount)
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