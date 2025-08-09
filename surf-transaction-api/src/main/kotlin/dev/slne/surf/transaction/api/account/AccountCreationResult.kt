package dev.slne.surf.transaction.api.account

import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component

@Serializable
sealed class AccountCreationResult(val message: suspend () -> Component) {
    
    @Serializable
    data class Success(val account: Account) : AccountCreationResult({
        buildText {
            append(account.asComponent())
            success(" wurde erfolgreich erstellt.")
        }
    })

    @Serializable
    data class Failure(val reason: FailureReason) : AccountCreationResult({
        buildText {
            error("Fehler beim Erstellen des Accounts: ")
            variableValue(reason.name)
        }
    })

    @Serializable
    enum class FailureReason {
        NAME_ALREADY_EXISTS,
    }
}