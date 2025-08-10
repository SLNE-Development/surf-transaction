package dev.slne.surf.transaction.paper.commands.currency.admin.subcommands

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.CommandAPIBukkit
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.kotlindsl.*
import dev.slne.surf.cloud.api.client.netty.packet.fireAndAwaitOrThrow
import dev.slne.surf.surfapi.bukkit.api.command.args.MiniMessageArgument
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.currency.Currency.Companion.CURRENCY_NAME_MAX_LENGTH
import dev.slne.surf.transaction.api.currency.Currency.Companion.CURRENCY_SYMBOL_MAX_LENGTH
import dev.slne.surf.transaction.api.currency.CurrencyScale
import dev.slne.surf.transaction.core.currency.CurrencyCreateResult
import dev.slne.surf.transaction.core.currency.CurrencyImpl
import dev.slne.surf.transaction.core.netty.packets.serverbound.ServerboundCreateCurrencyPacket
import dev.slne.surf.transaction.paper.commands.CommandPermission
import dev.slne.surf.transaction.paper.commands.arguments.currencyScaleArgument
import dev.slne.surf.transaction.paper.commands.arguments.getCurrencyScale
import dev.slne.surf.transaction.paper.plugin
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender

fun Argument<*>.currencyCreateCommand() = literalArgument("create") {
    withPermission(CommandPermission.CURRENCY_ADMIN_CREATE)

    stringArgument("name") {
        currencyScaleArgument("scale") {
            stringArgument("symbol") {
                doubleArgument("minimumAmount") {
                    argument(MiniMessageArgument("displayName")) {
                        argument(MiniMessageArgument("symbolDisplay")) {
                            anyExecutor { sender, args ->
                                create(
                                    sender,
                                    args.getUnchecked("name")!!,
                                    args.getCurrencyScale("scale"),
                                    args.getUnchecked("symbol")!!,
                                    args.getUnchecked("minimumAmount")!!,
                                    args.getUnchecked("displayName")!!,
                                    args.getUnchecked("symbolDisplay")!!
                                )
                            }
                        }
                    }
                }
            }
        }
    }

}

private fun create(
    sender: CommandSender,
    name: String,
    scale: CurrencyScale,
    symbol: String,
    minimumAmount: Double,
    displayName: Component,
    symbolDisplay: Component
) {
    val existingCurrency = Currency.byName(name)
    if (existingCurrency != null) {
        throw CommandAPIBukkit.failWithAdventureComponent(buildText {
            error("Currency ")
            append(existingCurrency.displayName)
            error(" already exists!")
        })
    }

    plugin.launch {
        val currency = CurrencyImpl(
            name = name,
            scale = scale,
            displayName = displayName,
            symbol = symbol,
            symbolDisplay = symbolDisplay,
            defaultCurrency = false,
            minimumAmount = minimumAmount.toBigDecimal()
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