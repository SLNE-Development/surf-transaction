package dev.slne.surf.transaction.core

import dev.slne.surf.database.DatabaseApi
import dev.slne.surf.surfapi.core.api.util.requiredService
import dev.slne.surf.transaction.core.currency.CurrencyServiceImpl
import dev.slne.surf.transaction.core.db.CreateTables
import dev.slne.surf.transaction.core.redis.RedisService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path

abstract class TransactionInstance {

    val databaseApi = DatabaseApi.create(dataPath)

    abstract val dataPath: Path

    open suspend fun load() {
        CreateTables.create()
        withContext(Dispatchers.IO) { RedisService.get().connect() }
        CurrencyServiceImpl.get().cacheCurrencies()
    }

    open suspend fun enable() {
    }

    open suspend fun disable() {
        withContext(Dispatchers.IO) { RedisService.get().disconnect() }
        databaseApi.shutdown()
    }

    companion object {
        val instance = requiredService<TransactionInstance>()
        fun get() = instance
    }
}