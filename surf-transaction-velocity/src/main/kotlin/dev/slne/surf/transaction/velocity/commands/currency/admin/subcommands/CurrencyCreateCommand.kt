package dev.slne.surf.transaction.velocity.commands.currency.admin.subcommands

import com.github.shynixn.mccoroutine.velocity.launch
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.*
import dev.slne.surf.cloud.api.client.netty.packet.fireAndAwaitOrThrow
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.velocity.api.command.args.miniMessageArgument
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.currency.Currency.Companion.CURRENCY_NAME_MAX_LENGTH
import dev.slne.surf.transaction.api.currency.Currency.Companion.CURRENCY_SYMBOL_MAX_LENGTH
import dev.slne.surf.transaction.api.currency.CurrencyScale
import dev.slne.surf.transaction.core.currency.CurrencyCreateResult
import dev.slne.surf.transaction.core.currency.CurrencyImpl
import dev.slne.surf.transaction.core.netty.packets.ServerboundCreateCurrencyPacket
import dev.slne.surf.transaction.velocity.plugin
import net.kyori.adventure.text.Component
import java.math.BigDecimal

fun CommandAPICommand.currencyCreateCommand() = subcommand("create") {
    withPermission("surf.transaction.currency.admin.create")

    stringArgument("name")
    multiLiteralArgument(
        "scale",
        *CurrencyScale.entries.map { it.name.lowercase() }.toTypedArray()
    )
    stringArgument("symbol")
    doubleArgument("minimumAmount")
    miniMessageArgument("displayName")
    miniMessageArgument("symbolDisplay")

    anyExecutor { sender, args ->
        val name: String by args
        val scale: String by args
        val symbol: String by args
        val minimumAmount: Double by args
        val displayName: Component by args
        val symbolDisplay: Component by args

        val currencyScale = CurrencyScale.valueOf(scale.uppercase())
        val existingCurrency = Currency.byName(name)

        if (existingCurrency != null) {
            sender.sendText {
                error("Currency ")
                append(existingCurrency.displayName)
                error(" already exists")
            }
        }

        plugin.container.launch {
            val currency = CurrencyImpl(
                name = name,
                scale = currencyScale,
                displayName = displayName,
                symbol = symbol,
                symbolDisplay = symbolDisplay,
                defaultCurrency = false,
                minimumAmount = BigDecimal.valueOf(minimumAmount)
            )

            val result = ServerboundCreateCurrencyPacket(currency).fireAndAwaitOrThrow().result

            when (result) {
                is CurrencyCreateResult.SUCCESS -> {
                    sender.sendText {
                        success("Currency $name created successfully")
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

                CurrencyCreateResult.CHANGED_DEFAULT_CURRENCY -> {
                    sender.sendText {
                        success("Currency $name created and set as default currency")
                    }
                }
            }
        }
    }
}