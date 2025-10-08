package dev.slne.surf.transaction.api.account.result

import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.java.SerializableUUID
import dev.slne.surf.surfapi.core.api.messages.builder.SurfComponentBuilder
import dev.slne.surf.transaction.api.util.ComponentResult
import kotlinx.serialization.Serializable

@Serializable
sealed class AccountResult : ComponentResult {

    data class NotFound(
        val accountId: SerializableUUID
    ) : AccountResult() {
        override val isSuccess = false

        override suspend fun SurfComponentBuilder.buildMessage() {
            error("Das Konto mit der Id ")
            variableValue(accountId.toString())
            error(" wurde nicht gefunden.")
        }
    }

}