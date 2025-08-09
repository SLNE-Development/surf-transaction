package dev.slne.surf.transaction.api.account

import kotlinx.serialization.Serializable

/**
 * Represents the result of an account creation operation.
 * This sealed class encapsulates both successful and failed account creation attempts.
 *
 * @property message A message describing the result of the account creation attempt.
 */
@Serializable
sealed class AccountCreationResult(val message: String) {

    /**
     * Represents a successful account creation result.
     *
     * @property account The newly created account.
     */
    @Serializable
    data class Success(val account: Account) :
        AccountCreationResult("Dein Account wurde erfolgreich erstellt.")

    /**
     * Represents a failure in account creation.
     *
     * This class includes a reason for the failure, which can be one of several predefined reasons.
     * @property reason The reason for the failure, represented by a [FailureReason] enum.
     */
    @Serializable
    data class Failure(val reason: FailureReason) :
        AccountCreationResult("Fehler beim Erstellen des Accounts: $reason")

    /**
     * Enum representing the possible reasons for a failure in account creation.
     */
    @Serializable
    enum class FailureReason {
        /**
         * The account name already exists.
         */
        NAME_ALREADY_EXISTS,

        /**
         * The account name is too long.
         */
        NAME_TOO_LONG,

        /**
         * The account name is too short.
         */
        NAME_TOO_SHORT,

        /**
         * The account name matches a UUID format.
         */
        NAME_IS_UUID
    }
}