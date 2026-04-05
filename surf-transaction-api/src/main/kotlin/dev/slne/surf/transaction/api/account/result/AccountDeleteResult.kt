package dev.slne.surf.transaction.api.account.result

import kotlinx.serialization.Serializable

/**
 * Represents the result of an account deletion attempt.
 */
@Serializable
enum class AccountDeleteResult {

    /**
     * The account was deleted successfully.
     */
    SUCCESS,

    /**
     * The account could not be deleted because it is marked as the default account.
     */
    DEFAULT_ACCOUNT_CANNOT_BE_DELETED,

    /**
     * The account to be deleted could not be found.
     */
    ACCOUNT_NOT_FOUND;
}
