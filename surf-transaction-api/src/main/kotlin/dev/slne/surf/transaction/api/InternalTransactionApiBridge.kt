package dev.slne.surf.transaction.api

import dev.slne.surf.surfapi.core.api.util.requiredService
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import org.springframework.context.ApplicationContext

@InternalTransactionApi
interface InternalTransactionApiBridge {
    val context: ApplicationContext

    companion object {
        val instance = requiredService<InternalTransactionApiBridge>()
    }
}