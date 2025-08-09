package dev.slne.surf.transaction.paper.commands.account.arguments

import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.scope
import dev.jorel.commandapi.CommandAPI
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.CustomArgument
import dev.jorel.commandapi.arguments.StringArgument
import dev.slne.surf.cloud.api.common.player.toCloudPlayer
import dev.slne.surf.surfapi.core.api.messages.adventure.sendText
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.user.accountByName
import dev.slne.surf.transaction.api.user.accounts
import dev.slne.surf.transaction.paper.plugin
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.future.future
import java.util.concurrent.CompletableFuture

class AccountArgument(nodeName: String) : CustomArgument<Deferred<Account?>, String>(
    StringArgument(nodeName),
    { info ->
        val sender = info.sender
        val cloudPlayer = sender.toCloudPlayer()
            ?: throw CommandAPI.failWithString("You must be a player to use this command.")

        val input = info.input
        val deferred = CompletableDeferred<Account?>()

        plugin.launch {
            val account = cloudPlayer.accountByName(input)

            if (account == null) {
                sender.sendText {
                    appendPrefix()

                    error("Du besitzt kein Konto mit dem Namen ")
                    variableValue(input)
                    error(".")
                }

                deferred.complete(null)
            } else {
                deferred.complete(account)
            }
        }

        deferred
    }
) {
    init {
        replaceSuggestions(ArgumentSuggestions.stringCollectionAsync { info ->
            val sender = info.sender
            val cloudPlayer = sender.toCloudPlayer()
                ?: return@stringCollectionAsync CompletableFuture.completedFuture(emptyList<String>())

            plugin.scope.future {
                cloudPlayer.accounts()
                    .sortedBy { it.name }
                    .map { it.name }
            }
        })
    }
}

inline fun CommandAPICommand.accountArgument(
    nodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandAPICommand = withArguments(AccountArgument(nodeName).apply {
    isOptional = optional
    block()
})