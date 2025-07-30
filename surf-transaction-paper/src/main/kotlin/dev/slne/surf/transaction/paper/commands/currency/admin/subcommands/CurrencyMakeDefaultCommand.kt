package dev.slne.surf.transaction.paper.commands.currency.admin.subcommands

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandTree
import dev.jorel.commandapi.kotlindsl.anyExecutor
import dev.jorel.commandapi.kotlindsl.literalArgument
import dev.slne.surf.cloud.api.client.netty.packet.fireAndAwaitOrThrow
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.currency.Currency.Companion.CURRENCY_NAME_MAX_LENGTH
import dev.slne.surf.transaction.api.currency.Currency.Companion.CURRENCY_SYMBOL_MAX_LENGTH
import dev.slne.surf.transaction.core.currency.CurrencyCreateResult
import dev.slne.surf.transaction.core.netty.packets.ServerboundMakeDefaultCurrencyPacket
import dev.slne.surf.transaction.paper.commands.CommandPermission
import dev.slne.surf.transaction.paper.commands.arguments.currencyArgument
import dev.slne.surf.transaction.paper.plugin
import org.bukkit.command.CommandSender

fun CommandTree.currencyMakeDefaultCommand() = literalArgument("makeDefault") {
    withPermission(CommandPermission.CURRENCY_ADMIN_MAKE_DEFAULT)

    currencyArgument("currencyName") {
        anyExecutor { sender, args ->
            makeDefault(sender, args.getUnchecked("currencyName")!!)
        }
    }
}

private fun makeDefault(sender: CommandSender, currency: Currency) {
    if (currency.defaultCurrency) {
        throw CommandAPI.failWithString("Currency '${currency.name}' is already the default currency.")
    }

    plugin.launch {
        val result = ServerboundMakeDefaultCurrencyPacket(currency).fireAndAwaitOrThrow().result

        when (result) {
            is CurrencyCreateResult.SUCCESS, CurrencyCreateResult.CHANGED_DEFAULT_CURRENCY -> {
                sender.sendText {
                    success("Currency ${currency.name} has been set as the default currency")
                }
            }

            CurrencyCreateResult.ALREADY_EXISTS -> {
                sender.sendText {
                    error("Currency with name ${currency.name} already exists")
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