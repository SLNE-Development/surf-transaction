package dev.slne.surf.transaction.core

import dev.slne.surf.surfapi.core.api.util.requiredService
import dev.slne.surf.transaction.core.currency.CoreCurrencyService
import dev.slne.surf.transaction.core.redis.RedisService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path

abstract class TransactionInstance {
    abstract val dataPath: Path

    open suspend fun load() {
        withContext(Dispatchers.IO) { RedisService.get().connect() }
        CoreCurrencyService.get().cacheCurrencies()
    }

    open suspend fun enable() {
    }

    open suspend fun disable() {
        withContext(Dispatchers.IO) { RedisService.get().disconnect() }
    }

    companion object {
        val instance = requiredService<TransactionInstance>()
        fun get() = instance
    }
}
