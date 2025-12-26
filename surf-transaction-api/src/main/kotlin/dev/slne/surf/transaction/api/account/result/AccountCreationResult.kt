package dev.slne.surf.transaction.api.account.result

import dev.slne.surf.transaction.api.account.Account

/**
 * Represents the result of an account creation attempt.
 *
 * Account creation can either succeed with a newly created [Account]
 * or fail with a specific [FailureReason].
 */
sealed interface AccountCreationResult {

    /**
     * Indicates that the account was created successfully.
     *
     * @param account the newly created account
     */
    data class Success(val account: Account) : AccountCreationResult

    /**
     * Indicates that the account creation failed.
     *
     * @param reason the reason why account creation failed
     */
    data class Failed(val reason: FailureReason) : AccountCreationResult

    /**
     * Describes possible reasons why account creation may fail.
     */
    enum class FailureReason {
        /**
         * An account with the given name already exists.
         */
        NAME_ALREADY_EXISTS,

        /**
         * The provided account name exceeds the maximum allowed length.
         */
        NAME_TOO_LONG,

        /**
         * The provided account name is shorter than the minimum required length.
         */
        NAME_TOO_SHORT,

        /**
         * The provided account name is a valid UUID and therefore not allowed.
         */
        NAME_IS_UUID;
    }
}