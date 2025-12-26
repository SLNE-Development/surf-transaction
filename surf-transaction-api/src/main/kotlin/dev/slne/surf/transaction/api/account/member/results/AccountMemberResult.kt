package dev.slne.surf.transaction.api.account.member.results

/**
 * Represents the result of an account member modification operation.
 *
 * This result is returned when adding or removing members from an account.
 */
enum class AccountMemberResult {

    /**
     * The operation completed successfully.
     */
    SUCCESS,

    /**
     * The operation did not change anything.
     *
     * This typically means the target was already in the desired state
     * (e.g. adding an existing member or removing a non-member).
     */
    NOTHING_CHANGED,

    /**
     * The account on which the operation was attempted could not be found.
     */
    ACCOUNT_NOT_FOUND;
}
