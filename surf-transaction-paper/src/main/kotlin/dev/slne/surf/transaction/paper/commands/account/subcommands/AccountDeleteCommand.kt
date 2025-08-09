package dev.slne.surf.transaction.paper.commands.account.subcommands

import com.github.shynixn.mccoroutine.folia.launch
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.kotlindsl.getValue
import dev.jorel.commandapi.kotlindsl.playerExecutor
import dev.jorel.commandapi.kotlindsl.subcommand
import dev.slne.surf.cloud.api.common.player.toCloudPlayer
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.account.AccountDeleteResult
import dev.slne.surf.transaction.api.user.deleteAccount
import dev.slne.surf.transaction.paper.commands.CommandPermission
import dev.slne.surf.transaction.paper.commands.account.arguments.accountArgument
import dev.slne.surf.transaction.paper.plugin
import kotlinx.coroutines.Deferred

fun CommandAPICommand.accountDeleteCommand() = subcommand("delete") {
    withPermission(CommandPermission.ACCOUNT_DELETE)

    accountArgument("account")

    playerExecutor { player, args ->
        val account: Deferred<Account?> by args

        plugin.launch {
            val account = account.await() ?: return@launch
            val cloudPlayer = player.toCloudPlayer() ?: return@launch

            val result = cloudPlayer.deleteAccount(account)
            val message = result.message()

            cloudPlayer.sendText {
                appendPrefix()
                append(message)
            }
        }
    }
}