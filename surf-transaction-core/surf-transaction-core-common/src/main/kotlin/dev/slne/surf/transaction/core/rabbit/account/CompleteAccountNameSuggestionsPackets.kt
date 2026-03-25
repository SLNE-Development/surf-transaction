package dev.slne.surf.transaction.core.rabbit.account

import dev.slne.surf.rabbitmq.packet.RabbitRequestPacket
import dev.slne.surf.rabbitmq.packet.RabbitResponsePacket
import kotlinx.serialization.Serializable

@Serializable
class CompleteAccountNameSuggestionsRequest(val input: String, val maxSuggestions: Int) :
    RabbitRequestPacket<CompleteAccountNameSuggestionsResponse>()

@Serializable
class CompleteAccountNameSuggestionsResponse(val suggestions: List<String>) : RabbitResponsePacket()
