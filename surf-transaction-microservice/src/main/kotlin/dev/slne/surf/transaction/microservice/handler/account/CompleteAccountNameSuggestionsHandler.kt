package dev.slne.surf.transaction.microservice.handler.account

import dev.slne.surf.rabbitmq.api.handler.RabbitHandler
import dev.slne.surf.transaction.core.common.protocol.account.suggestion.CompleteAccountNameSuggestionsRequestPacket
import dev.slne.surf.transaction.core.common.protocol.account.suggestion.CompleteAccountNameSuggestionsResponsePacket
import dev.slne.surf.transaction.microservice.db.account.AccountRepository
import kotlinx.coroutines.launch

object CompleteAccountNameSuggestionsHandler {

    @RabbitHandler
    fun handleCompleteAccountNameSuggestions(request: CompleteAccountNameSuggestionsRequestPacket) {
        val (input, maxSuggestions) = request
        request.launch {
            val completions = AccountRepository.completeAccountNameSuggestions(input, maxSuggestions)
            request.respond(CompleteAccountNameSuggestionsResponsePacket(completions))
        }
    }
}