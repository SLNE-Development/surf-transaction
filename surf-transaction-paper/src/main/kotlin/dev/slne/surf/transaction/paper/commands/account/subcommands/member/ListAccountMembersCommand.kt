package dev.slne.surf.transaction.paper.commands.account.subcommands.member

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.surfapi.core.api.font.toSmallCaps
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.surfapi.core.api.messages.pagination.Pagination
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.paper.commands.CommandPermission
import dev.slne.surf.transaction.paper.commands.account.arguments.accountArgument
import dev.slne.surf.transaction.paper.plugin
import kotlinx.coroutines.Deferred
import net.kyori.adventure.text.Component

private typealias MemberDisplayName = Pair<OfflineCloudPlayer, Component>

private val pagination = Pagination<MemberDisplayName> {
    title {
        info("Account Mitglieder".toSmallCaps())
    }
    rowRenderer { (player, displayName), index ->
        listOf(displayName)
    }
}

fun CommandAPICommand.listAccountMemberCommand() = subcommand("list") {
    withPermission(CommandPermission.ACCOUNT_MEMBER_LIST)

    accountArgument("account")

    playerExecutor { sender, args ->
        val account: Deferred<Account?> by args

        plugin.launch {
            val acc = account.await() ?: return@launch
            val membersToDisplayName = acc.members.map { it to it.displayName() }

            sender.sendText {
                append(pagination.renderComponent(membersToDisplayName))
            }
        }
    }
}