package dev.slne.surf.transaction.paper.commands.account.arguments

import com.github.shynixn.mccoroutine.folia.scope
import dev.jorel.commandapi.CommandAPICommand
import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.arguments.StringArgument
import dev.slne.surf.api.core.command.args.awaitingOrNull
import dev.slne.surf.api.core.service.PlayerLookupService
import dev.slne.surf.api.core.util.mapAsync
import dev.slne.surf.api.paper.command.args.SuspendCustomArgument
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.paper.plugin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.future.future
import java.util.*

class AccountMemberArgument(nodeName: String, private val accountNodeName: String) :
    SuspendCustomArgument<UUID, String>(StringArgument(nodeName)) {
    init {
        replaceSuggestions(ArgumentSuggestions.stringCollectionAsync { info ->
            val args = info.previousArgs
            plugin.scope.future {
                val account =
                    runCatching { args.awaitingOrNull<Account>(accountNodeName) }.getOrNull()

                if (account == null) {
                    return@future emptyList()
                }

                account.members.mapAsync { PlayerLookupService.getUsername(it) }
            }
        })
    }

    override suspend fun CoroutineScope.parse(info: CustomArgumentInfo<String>): UUID {
        val playerName = info.input
        val uuid = PlayerLookupService.getUuid(playerName)
            ?: throw CustomArgumentException.fromMessageBuilder(
                MessageBuilder()
                    .append("Das Mitglied ")
                    .appendArgInput()
                    .append(" existiert nicht.")
            )

        val account = info.previousArgs.awaitingOrNull<Account>(accountNodeName)
            ?: throw CustomArgumentException.fromString("Das Konto wurde nicht gefunden.")

        if (!account.isMember(uuid)) {
            throw CustomArgumentException.fromMessageBuilder(
                MessageBuilder()
                    .appendArgInput()
                    .append(" ist kein Mitglied dieses Kontos.")
            )
        }

        return uuid
    }
}

inline fun CommandAPICommand.accountMemberArgument(
    nodeName: String,
    accountNodeName: String,
    optional: Boolean = false,
    block: Argument<*>.() -> Unit = {}
): CommandAPICommand = withArguments(AccountMemberArgument(nodeName, accountNodeName).apply {
    this.isOptional = optional
    block()
})