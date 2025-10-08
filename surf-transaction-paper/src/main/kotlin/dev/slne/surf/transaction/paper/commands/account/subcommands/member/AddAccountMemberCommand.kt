package dev.slne.surf.transaction.paper.commands.account.subcommands.member

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.entitySelectorArgumentOnePlayer
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.paper.commands.CommandPermission
import dev.slne.surf.transaction.paper.commands.account.arguments.accountArgument
import dev.slne.surf.transaction.paper.plugin
import kotlinx.coroutines.Deferred
import org.bukkit.entity.Player

fun CommandAPICommand.addAccountMemberCommand() = subcommand("add") {
    withPermission(CommandPermission.ACCOUNT_MEMBER_ADD)

    accountArgument("account")
    entitySelectorArgumentOnePlayer("target")

    playerExecutor { sender, args ->
        val account: Deferred<Account?> by args
        val target: Player by args

        plugin.launch {
            val acc = account.await() ?: return@launch
            val executorPlayer = OfflineCloudPlayer[sender.uniqueId]
            val targetPlayer = OfflineCloudPlayer[target.uniqueId]

            val result = acc.addMember(
                executorPlayer,
                targetPlayer
            )

            sender.sendText {
                append(result.asComponent())
            }
        }
    }
}