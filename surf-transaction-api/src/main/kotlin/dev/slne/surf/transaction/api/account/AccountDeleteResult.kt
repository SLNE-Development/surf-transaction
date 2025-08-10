package dev.slne.surf.transaction.api.account

import kotlinx.serialization.Serializable

/**
 * Represents the result of an account deletion operation.
 * This sealed class encapsulates both successful and failed account deletion attempts.
 *
 * @property message A message describing the result of the account deletion attempt.
 */
@Serializable
sealed class AccountDeleteResult(val message: String) {

    /**
     * Represents a successful account deletion result.
     *
     * @property account The account that was successfully deleted.
     */
    @Serializable
    data class Success(val account: Account) :
        AccountDeleteResult("Dein Account wurde erfolgreich gelöscht.")

    /**
     * Represents a failure in account deletion.
     * This class includes a reason for the failure, which can be one of several predefined reasons.
     *
     * @property reason The reason for the failure, represented by a [FailureReason] enum
     */
    @Serializable
    data class Failure(val reason: FailureReason) :
        AccountDeleteResult("Fehler beim Löschen des Accounts: $reason")

    /**
     * Represents the possible reasons for a failure in account deletion.
     */
    @Serializable
    enum class FailureReason {
        /**
         * The account could not be found.
         */
        ACCOUNT_NOT_FOUND,
    }
}