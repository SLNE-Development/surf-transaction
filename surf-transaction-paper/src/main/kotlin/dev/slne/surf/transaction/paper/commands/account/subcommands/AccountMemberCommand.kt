package dev.slne.surf.transaction.paper.commands.account.subcommands

import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.transaction.paper.commands.CommandPermission
import dev.slne.surf.transaction.paper.commands.account.subcommands.member.addAccountMemberCommand
import dev.slne.surf.transaction.paper.commands.account.subcommands.member.listAccountMemberCommand
import dev.slne.surf.transaction.paper.commands.account.subcommands.member.removeAccountMemberCommand

fun CommandAPICommand.accountMemberCommand() = subcommand("member") {
    withPermission(CommandPermission.ACCOUNT_MEMBER)
    
    addAccountMemberCommand()
    removeAccountMemberCommand()
    listAccountMemberCommand()
}