package dev.slne.surf.transaction.core.client

import dev.slne.surf.transaction.core.common.TransactionInstance
import dev.slne.surf.transaction.core.client.currency.CurrencyServiceImpl
import dev.slne.surf.transaction.core.client.redis.RedisService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path

abstract class ClientTransactionalInstance : TransactionInstance() {
    abstract val dataPath: Path

    override suspend fun load() {
        super.load()

        withContext(Dispatchers.IO) { RedisService.get().connect() }
        CurrencyServiceImpl.get().cacheCurrencies()
    }

    override suspend fun disable() {
        super.disable()

        withContext(Dispatchers.IO) { RedisService.get().disconnect() }
    }

    companion object {
        fun get() = TransactionInstance.get() as ClientTransactionalInstance
    }
}