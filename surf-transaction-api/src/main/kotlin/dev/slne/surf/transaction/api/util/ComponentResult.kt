package dev.slne.surf.transaction.api.util

import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.java.SerializableUUID
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.surfapi.core.api.messages.builder.SurfComponentBuilder
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component

interface ComponentResult {

    val isSuccess: Boolean
    val isError: Boolean get() = !isSuccess

    suspend fun SurfComponentBuilder.buildMessage()

    suspend fun asComponent(): Component {
        val componentBuilder = SurfComponentBuilder.builder()
        componentBuilder.buildMessage()
        return componentBuilder.build()
    }

    @Serializable
    data object EmptySuccess : ComponentResult {
        override val isSuccess = true

        override suspend fun SurfComponentBuilder.buildMessage() {}
    }

    @Serializable
    data class PlayerNotFound(val playerUuid: SerializableUUID) : ComponentResult {
        override val isSuccess = false

        override suspend fun SurfComponentBuilder.buildMessage() {
            val player = OfflineCloudPlayer[playerUuid]

            append(player.displayName())
            error(" konnte nicht gefunden werden.")
        }
    }

}