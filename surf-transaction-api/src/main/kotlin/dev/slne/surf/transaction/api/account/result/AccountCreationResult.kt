package dev.slne.surf.transaction.api.account.result

import dev.slne.surf.surfapi.core.api.messages.builder.SurfComponentBuilder
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.util.ComponentResult
import kotlinx.serialization.Serializable

/**
 * Represents the result of an account creation operation.
 * This sealed class encapsulates both successful and failed account creation attempts.
 *
 * @property message A message describing the result of the account creation attempt.
 */
@Serializable
sealed class AccountCreationResult : ComponentResult {

    /**
     * Represents a successful account creation result.
     *
     * @property account The newly created account.
     */
    @Serializable
    data class Success(val account: Account) : AccountCreationResult() {
        override val isSuccess = true

        override suspend fun SurfComponentBuilder.buildMessage() {
            success("Das Konto ")
            variableValue(account.name)
            success(" wurde erfolgreich erstellt.")
        }
    }

    /**
     * Represents a failure in account creation.
     *
     * This class includes a reason for the failure, which can be one of several predefined reasons.
     * @property reason The reason for the failure, represented by a [FailureReason] enum.
     */
    @Serializable
    data class Failure(val reason: FailureReason) : AccountCreationResult() {
        override val isSuccess = false

        override suspend fun SurfComponentBuilder.buildMessage() {
            error("Das Konto konnte nicht erstellt werden. Grund: ")
            reason.message(this)
        }
    }

    /**
     * Enum representing the possible reasons for a failure in account creation.
     */
    @Serializable
    enum class FailureReason(
        val message: SurfComponentBuilder.() -> Unit
    ) {
        /**
         * The account name already exists.
         */
        NAME_ALREADY_EXISTS(
            message = {
                error("Ein Konto mit diesem Namen existiert bereits.")
            }
        ),

        /**
         * The account name is too long.
         */
        NAME_TOO_LONG(
            message = {
                error("Der Kontoname ist zu lang. Er darf maximal ")
                variableValue(Account.Companion.MAX_NAME_LENGTH)
                error(" Zeichen lang sein.")
            }
        ),

        /**
         * The account name is too short.
         */
        NAME_TOO_SHORT(
            message = {
                error("Der Kontoname ist zu kurz. Er muss mindestens ")
                variableValue(Account.Companion.MIN_NAME_LENGTH)
                error(" Zeichen lang sein.")
            }
        ),

        /**
         * The account name matches a UUID format.
         */
        NAME_IS_UUID(
            message = {
                error("Der Kontoname darf kein UUID-Format haben.")
            }
        ),
    }
}