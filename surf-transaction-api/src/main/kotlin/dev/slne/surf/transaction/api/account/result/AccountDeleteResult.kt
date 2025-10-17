package dev.slne.surf.transaction.api.account.result

import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.java.SerializableUUID
import dev.slne.surf.surfapi.core.api.messages.builder.SurfComponentBuilder
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.util.ComponentResult
import kotlinx.serialization.Serializable

@Serializable
sealed class AccountDeleteResult : ComponentResult {

    /**
     * Represents a successful account deletion result.
     *
     * @property account The account that was successfully deleted.
     */
    @Serializable
    data class Success(val account: Account) : AccountDeleteResult() {
        override val isSuccess = true

        override suspend fun SurfComponentBuilder.buildMessage() {
            success("Das Konto ")
            variableValue(account.name)
            success(" wurde erfolgreich gelöscht.")
        }
    }

    @Serializable
    data class NotFound(
        val accountId: SerializableUUID
    ) : AccountDeleteResult() {
        override val isSuccess = false

        override suspend fun SurfComponentBuilder.buildMessage() {
            error("Das Konto mit der Id ")
            variableValue(accountId.toString())
            error(" wurde nicht gefunden.")
        }
    }

    /**
     * Represents a failure in account deletion.
     * This class includes a reason for the failure, which can be one of several predefined reasons.
     *
     * @property reason The reason for the failure, represented by a [FailureReason] enum
     */
    @Serializable
    data class Failure(val reason: FailureReason) : AccountDeleteResult() {
        override val isSuccess = false

        override suspend fun SurfComponentBuilder.buildMessage() {
            error("Das Konto konnte nicht gelöscht werden. Grund: ")
            reason.message(this)
        }
    }

    /**
     * Represents the possible reasons for a failure in account deletion.
     */
    @Serializable
    enum class FailureReason(
        val message: SurfComponentBuilder.() -> Unit
    )
}