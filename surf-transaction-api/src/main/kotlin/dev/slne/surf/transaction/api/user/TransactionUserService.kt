package dev.slne.surf.transaction.api.user

import dev.slne.surf.surfapi.core.api.util.requiredService
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import java.util.UUID

@InternalTransactionApi
interface TransactionUserService {
    fun byUuid(uuid: UUID): TransactionUser

    companion object {
        val instance = requiredService<TransactionUserService>()
    }
}