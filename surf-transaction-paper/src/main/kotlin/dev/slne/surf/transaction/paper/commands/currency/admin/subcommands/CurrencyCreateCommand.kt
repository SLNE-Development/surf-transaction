package dev.slne.surf.transaction.paper.commands.currency.admin.subcommands

import dev.jorel.commandapi.CommandAPIPaper
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.kotlindsl.argument
import dev.jorel.commandapi.kotlindsl.doubleArgument
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.jorel.commandapi.kotlindsl.stringArgument
import dev.slne.surf.surfapi.bukkit.api.command.args.MiniMessageArgument
import dev.slne.surf.surfapi.bukkit.api.command.executors.anyExecutorSuspend
import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.currency.CurrencyScale
import dev.slne.surf.transaction.core.common.component.Components
import dev.slne.surf.transaction.core.common.currency.CurrencyImpl
import dev.slne.surf.transaction.core.common.currency.CurrencyServiceImpl
import dev.slne.surf.transaction.paper.commands.CommandPermission
import dev.slne.surf.transaction.paper.commands.arguments.currencyScaleArgument
import dev.slne.surf.transaction.paper.commands.arguments.getCurrencyScale
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
                            anyExecutorSuspend { sender, args ->
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

private suspend fun create(
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
        throw CommandAPIPaper.failWithAdventureComponent(buildText {
            error("Die Währung ")
            append(existingCurrency)
            error(" existiert bereits!")
        })
    }

    val currency = CurrencyImpl(
        name = name,
        scale = scale,
        displayName = displayName,
        symbol = symbol,
        symbolDisplay = symbolDisplay,
        minimumAmount = minimumAmount.toBigDecimal()
    )

    val result = CurrencyServiceImpl.get().createCurrency(currency)

    sender.sendText {
        appendPrefix()
        append(Components.Currency.formatCreateResult(currency, result))
    }
}