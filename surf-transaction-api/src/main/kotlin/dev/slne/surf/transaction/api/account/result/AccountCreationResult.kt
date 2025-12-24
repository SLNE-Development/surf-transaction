package dev.slne.surf.transaction.api.account.result

import dev.slne.surf.transaction.api.account.Account

sealed interface AccountCreationResult {

    data class Success(val account: Account) : AccountCreationResult
    data class Failed(val reason: FailureReason) : AccountCreationResult

    enum class FailureReason {
        NAME_ALREADY_EXISTS,
        NAME_TOO_LONG,
        NAME_TOO_SHORT,
        NAME_IS_UUID;
    }
}