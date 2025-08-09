package dev.slne.surf.transaction.api.account

import kotlinx.serialization.Serializable

@Serializable
sealed class AccountCreationResult(val message: String) {

    @Serializable
    data class Success(val account: Account) :
        AccountCreationResult("Dein Account wurde erfolgreich erstellt.")

    @Serializable
    data class Failure(val reason: FailureReason) :
        AccountCreationResult("Fehler beim Erstellen des Accounts: $reason")

    @Serializable
    enum class FailureReason {
        NAME_ALREADY_EXISTS,
        NAME_TOO_LONG,
        NAME_TOO_SHORT,
        NAME_IS_UUID
    }
}