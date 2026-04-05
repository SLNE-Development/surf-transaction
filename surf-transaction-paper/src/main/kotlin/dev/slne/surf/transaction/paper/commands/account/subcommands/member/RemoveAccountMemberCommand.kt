package dev.slne.surf.transaction.paper.commands.account.subcommands.member

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.api.core.command.args.awaiting
import dev.slne.surf.api.core.messages.adventure.sendText
import dev.slne.surf.api.paper.command.executors.playerExecutorSuspend
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.core.common.component.Components
import dev.slne.surf.transaction.paper.commands.CommandPermission
import dev.slne.surf.transaction.paper.commands.account.arguments.accountArgument
import dev.slne.surf.transaction.paper.commands.account.arguments.accountMemberArgument
import java.util.*

fun CommandAPICommand.removeAccountMemberCommand() = subcommand("remove") {
    withPermission(CommandPermission.ACCOUNT_MEMBER_REMOVE)

    accountArgument("account")
    accountMemberArgument("targetUuid", accountNodeName = "account")

    playerExecutorSuspend { sender, args ->
        val account = args.awaiting<Account>("account")
        val targetUuid: UUID by args

        val result = account.removeMember(sender.uniqueId, targetUuid)

        sender.sendText {
            appendInfoPrefix()
            append(Components.Account.Member.formatResult(result, targetUuid, added = false))
        }
    }
}