package dev.slne.surf.transaction.api.account

import kotlinx.serialization.Serializable

@Serializable
sealed class AccountDeleteResult(val message: String) {

    @Serializable
    data class Success(val account: Account) :
        AccountDeleteResult("Dein Account wurde erfolgreich gelöscht.")

    @Serializable
    data class Failure(val reason: FailureReason) :
        AccountDeleteResult("Fehler beim Löschen des Accounts: $reason")

    @Serializable
    enum class FailureReason {
        ACCOUNT_NOT_FOUND,
    }
}