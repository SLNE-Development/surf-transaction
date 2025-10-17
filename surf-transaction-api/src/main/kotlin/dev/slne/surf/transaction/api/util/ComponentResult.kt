package dev.slne.surf.transaction.api.util

import dev.slne.surf.surfapi.core.api.messages.builder.SurfComponentBuilder
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

}