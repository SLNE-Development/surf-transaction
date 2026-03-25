package dev.slne.surf.transaction.core.common.protocol.account.suggestion

import dev.slne.surf.rabbitmq.api.packet.RabbitResponsePacket
import kotlinx.serialization.Serializable

@Serializable
data class CompleteAccountNameSuggestionsResponsePacket(val completions: List<String>) : RabbitResponsePacket()