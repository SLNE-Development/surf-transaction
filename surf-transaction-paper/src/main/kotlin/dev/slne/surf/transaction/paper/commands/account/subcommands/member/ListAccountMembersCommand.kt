package dev.slne.surf.transaction.paper.commands.account.subcommands.member

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.api.core.command.args.awaiting
import dev.slne.surf.api.core.font.toSmallCaps
import dev.slne.surf.api.core.messages.pagination.Pagination
import dev.slne.surf.api.core.util.mapAsync
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.core.common.component.Components
import dev.slne.surf.transaction.paper.commands.CommandPermission
import dev.slne.surf.transaction.paper.commands.account.arguments.accountArgument


private val pagination = Pagination {
    title {
        info("Account Mitglieder".toSmallCaps())
    }
    rowRenderer { displayName, _ ->
        listOf(displayName)
    }
}

fun CommandAPICommand.listAccountMemberCommand() = subcommand("list") {
    withPermission(CommandPermission.ACCOUNT_MEMBER_LIST)

    accountArgument("account")

    playerExecutorSuspend { sender, args ->
        val account = args.awaiting<Account>("account")
        val memberNames = account.members.mapAsync(Components::usernameOrUuidComponent)

        sender.sendMessage(pagination.renderComponent(memberNames))
    }
}
