package dev.slne.surf.transaction.paper.commands.balance

import dev.jorel.commandapi.arguments.AsyncPlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.argument
import dev.jorel.commandapi.kotlindsl.commandTree
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.command.executors.anyExecutorSuspend
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.api.paper.command.util.awaitAsyncPlayerProfile
import dev.slne.surf.api.paper.command.util.idOrThrow
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.user.TransactionUser
import dev.slne.surf.transaction.core.common.component.Components
import dev.slne.surf.transaction.paper.commands.CommandPermission
import dev.slne.surf.transaction.paper.commands.arguments.currencyArgument
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

fun balanceCommand() = commandTree("balance") {
    withPermission(CommandPermission.BALANCE)
    withAliases("bal", "konto", "money")

    currencyArgument("currency") {
        playerExecutorSuspend { sender, args ->
            balance(
                sender,
                args.getUnchecked("currency")!!,
                sender.uniqueId
            )
        }

        argument(AsyncPlayerProfileArgument("player")) {
            withPermission(CommandPermission.BALANCE_OTHER)

            anyExecutorSuspend { sender, args ->
                balance(
                    sender,
                    args.getUnchecked("currency")!!,
                    args.awaitAsyncPlayerProfile("player").idOrThrow()
                )
            }
        }
    }
}

private suspend fun balance(
    sender: CommandSender,
    currency: Currency,
    queryTarget: UUID
) {
    val balance = TransactionUser.byUuid(queryTarget).balance(currency)

    sender.sendText {
        appendInfoPrefix()

        if (sender is Player && queryTarget == sender.uniqueId) {
            info("Dein Kontostand beträgt ")
        } else {
            info("Der Kontostand von ")
            append(Components.usernameOrUuidComponent(queryTarget))
            info(" beträgt ")
        }

        append(currency.format(balance))
        info(".")
    }
}