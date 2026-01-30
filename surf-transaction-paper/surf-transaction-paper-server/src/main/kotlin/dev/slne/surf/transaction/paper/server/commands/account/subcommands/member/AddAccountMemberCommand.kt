package dev.slne.surf.transaction.paper.server.commands.account.subcommands.member

import com.destroystokyo.paper.profile.PlayerProfile
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.AsyncPlayerProfileArgument
import dev.jorel.commandapi.kotlindsl.argument
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.surfapi.bukkit.api.command.executors.playerExecutorSuspend
import dev.slne.surf.surfapi.core.api.command.args.awaiting
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.core.component.Components
import dev.slne.surf.transaction.paper.server.commands.CommandPermission
import dev.slne.surf.transaction.paper.server.commands.account.arguments.accountArgument
import kotlinx.coroutines.future.await
import java.util.concurrent.CompletableFuture

fun CommandAPICommand.addAccountMemberCommand() = subcommand("add") {
    withPermission(CommandPermission.ACCOUNT_MEMBER_ADD)

    accountArgument("account")
    argument(AsyncPlayerProfileArgument("target"))

    playerExecutorSuspend { sender, args ->
        val account = args.awaiting<Account>("account")
        val target =
            args.getUnchecked<CompletableFuture<List<PlayerProfile>>>("target")!!.await().first()

        val targetUuid =
            target.id ?: throw CommandAPI.failWithString("Es wurde keine UUID gefunden.")

        val result = account.addMember(
            sender.uniqueId,
            targetUuid
        )

        sender.sendText {
            appendPrefix()
            append(Components.Account.Member.formatResult(result, targetUuid, added = true))
        }
    }
}