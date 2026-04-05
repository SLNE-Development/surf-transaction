package dev.slne.surf.transaction.api.transaction

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.transaction.api.util.InternalTransactionApi

@InternalTransactionApi
interface TransactionService {

    companion object : TransactionService by instance {
        val INSTANCE get() = instance
    }
}

private val instance = requiredService<TransactionService>()
