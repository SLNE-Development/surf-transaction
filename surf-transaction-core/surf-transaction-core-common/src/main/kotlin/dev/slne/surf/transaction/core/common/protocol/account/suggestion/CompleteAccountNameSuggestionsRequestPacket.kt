package dev.slne.surf.transaction.core.common.protocol.account.suggestion

import dev.slne.surf.rabbitmq.api.packet.RabbitRequestPacket
import kotlinx.serialization.Serializable

@Serializable
data class CompleteAccountNameSuggestionsRequestPacket(val input: String, val maxSuggestions: Int) :
    RabbitRequestPacket<CompleteAccountNameSuggestionsResponsePacket>()