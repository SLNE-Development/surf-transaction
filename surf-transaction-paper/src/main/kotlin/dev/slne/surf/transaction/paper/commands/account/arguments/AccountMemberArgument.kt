package dev.slne.surf.transaction.paper.commands.account.arguments

import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.scope
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.jorel.commandapi.kotlindsl.getValue
import dev.slne.surf.cloud.api.client.paper.command.args.OfflineCloudPlayerArgument
import dev.slne.surf.cloud.api.common.player.CloudPlayerManager
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.paper.plugin
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.future.future
import java.util.*

class AccountMemberArgument(nodeName: String) : CustomArgument<Deferred<UUID?>, String>(
    StringArgument(nodeName),
    { info ->
        val input = info.input
        val deferred = CompletableDeferred<UUID?>()

        plugin.launch {
            // FIXME: This should use the offline cloud player method which yet doesnt exist
            val uuid = CloudPlayerManager.getPlayer(input)?.uuid

            deferred.complete(uuid)
        }

        deferred
    }
) {
    init {
        replaceSuggestions(ArgumentSuggestions.stringCollectionAsync { info ->
            val args = info.previousArgs
            val account: Deferred<Account?> by args

            plugin.scope.future {
                val acc = account.await() ?: return@future listOf<String>()

                // FIXME: Also fix this, see above
                acc.members.map { it.uuid.toString() }
            }
        })
    }
}

inline fun CommandAPICommand.accountMemberArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandAPICommand = withArguments(OfflineCloudPlayerArgument(nodeName).apply {
    this.isOptional = optional

    block()
})