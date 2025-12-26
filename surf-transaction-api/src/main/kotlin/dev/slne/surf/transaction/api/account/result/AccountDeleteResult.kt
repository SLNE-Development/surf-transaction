package dev.slne.surf.transaction.api.account.result

enum class AccountDeleteResult {
    SUCCESS,
    DEFAULT_ACCOUNT_CANNOT_BE_DELETED,
    ACCOUNT_NOT_FOUND;
}