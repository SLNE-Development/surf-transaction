package dev.slne.surf.transaction.microservice

import com.google.auto.service.AutoService
import dev.slne.surf.database.DatabaseApi
import dev.slne.surf.transaction.core.TransactionInstance
import dev.slne.surf.transaction.core.currency.CoreCurrencyService
import dev.slne.surf.transaction.core.db.CreateTables
import dev.slne.surf.transaction.core.redis.RedisService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path

@AutoService(TransactionInstance::class)
class TransactionMicroserviceInstance : TransactionInstance() {
    override val dataPath: Path = Path.of("data")
    val databaseApi = DatabaseApi.create(dataPath)

    override suspend fun load() {
        CreateTables.create()
        withContext(Dispatchers.IO) { RedisService.get().connect() }
        CoreCurrencyService.get().cacheCurrencies()
    }

    override suspend fun disable() {
        withContext(Dispatchers.IO) { RedisService.get().disconnect() }
        databaseApi.shutdown()
    }
}
