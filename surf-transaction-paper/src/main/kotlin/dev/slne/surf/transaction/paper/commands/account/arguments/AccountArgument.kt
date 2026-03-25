package dev.slne.surf.transaction.paper.commands.account.arguments

import com.github.shynixn.mccoroutine.folia.scope
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.SuggestionInfo
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.slne.surf.surfapi.bukkit.api.command.args.SuspendCustomArgument
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.core.client.account.AccountServiceImpl
import dev.slne.surf.transaction.paper.plugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import org.bukkit.command.CommandSender
import java.util.concurrent.CompletableFuture

class AccountArgument(nodeName: String) :
    SuspendCustomArgument<Account, String>(StringArgument(nodeName)) {
    init {
        replaceSuggestions(AccountSuggestions)
    }

    override suspend fun CoroutineScope.parse(info: CustomArgumentInfo<String>): Account {
        val input = info.input
        val account = Account.byName(input) ?: throw CustomArgumentException.fromMessageBuilder(
            MessageBuilder()
                .append("Das Konto ")
                .appendArgInput()
                .append(" existiert nicht.")
        )

        return account
    }

    object AccountSuggestions : ArgumentSuggestions<CommandSender> {
        override fun suggest(
            info: SuggestionInfo<CommandSender>,
            builder: SuggestionsBuilder
        ): CompletableFuture<Suggestions> = plugin.scope.future {
            val currentInput = info.currentInput
            val suggestions =
                AccountServiceImpl.get().completeAccountNameSuggestions(currentInput, 100)

            for (suggestion in suggestions) {
                builder.suggest(suggestion)
            }

            builder.build()
        }
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