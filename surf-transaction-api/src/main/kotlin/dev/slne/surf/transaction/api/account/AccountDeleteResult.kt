package dev.slne.surf.transaction.api.account

import dev.slne.surf.surfapi.core.api.messages.adventure.buildText
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component

@Serializable
sealed class AccountDeleteResult(val message: suspend () -> Component) {

    @Serializable
    data class Success(val account: Account) : AccountDeleteResult({
        buildText {
            append(account.asComponent())
            success(" wurde erfolgreich gelöscht.")
        }
    })

    @Serializable
    data class Failure(val reason: FailureReason) : AccountDeleteResult({
        buildText {
            error("Fehler beim Löschen des Accounts: ")
            variableValue(reason.name)
        }
    })

    @Serializable
    enum class FailureReason {
        ACCOUNT_NOT_FOUND,
    }
}