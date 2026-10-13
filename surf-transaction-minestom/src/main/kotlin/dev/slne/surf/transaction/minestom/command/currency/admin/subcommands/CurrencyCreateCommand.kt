package dev.slne.surf.transaction.minestom.command.currency.admin.subcommands

import dev.slne.surf.api.minestom.command.CommandAPI
import dev.slne.surf.api.minestom.command.argument.Argument
import dev.slne.surf.api.minestom.command.dsl.anyExecutorSuspend
import dev.slne.surf.api.minestom.command.dsl.doubleArgument
import dev.slne.surf.api.minestom.command.dsl.literalArgument
import dev.slne.surf.api.minestom.command.dsl.stringArgument
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.minestom.command.args.miniMessageArgument
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.currency.CurrencyScale
import dev.slne.surf.transaction.core.client.command.TransactionPermissions
import dev.slne.surf.transaction.core.client.component.ClientComponents
import dev.slne.surf.transaction.core.client.currency.CurrencyServiceImpl
import dev.slne.surf.transaction.core.common.component.Components
import dev.slne.surf.transaction.core.common.currency.CurrencyImpl
import dev.slne.surf.transaction.minestom.command.arguments.currencyScaleArgument
import dev.slne.surf.transaction.minestom.command.arguments.getCurrencyScale
import net.kyori.adventure.text.Component
import net.minestom.server.command.CommandSender

fun Argument<String>.currencyCreateCommand() = literalArgument("create") {
    withPermission(TransactionPermissions.CURRENCY_ADMIN_CREATE)

    stringArgument("name") {
        currencyScaleArgument("scale") {
            stringArgument("symbol") {
                doubleArgument("minimumAmount") {
                    miniMessageArgument("displayName") {
                        miniMessageArgument("symbolDisplay") {
                            anyExecutorSuspend { sender, args ->
                                create(
                                    sender,
                                    args.get("name"),
                                    args.getCurrencyScale("scale"),
                                    args.get("symbol"),
                                    args.get("minimumAmount"),
                                    args.get("displayName"),
                                    args.get("symbolDisplay")
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
        CommandAPI.failWithMessage(
            ClientComponents.CurrencyMessages.alreadyExists(existingCurrency)
        )
    }

    val currency = CurrencyImpl(
        name = name,
        scale = scale,
        displayName = displayName,
        symbol = symbol,
        symbolDisplay = symbolDisplay,
        minimumAmount = minimumAmount.toBigDecimal()
    )

    val result = CurrencyServiceImpl.INSTANCE.createCurrency(currency)

    sender.sendText {
        append(Components.Currency.formatCreateResult(currency, result))
    }
}
