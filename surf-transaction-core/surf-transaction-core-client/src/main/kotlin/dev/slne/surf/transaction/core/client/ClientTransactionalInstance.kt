package dev.slne.surf.transaction.core.client

import dev.slne.surf.rabbitmq.api.ClientRabbitMQApi
import dev.slne.surf.transaction.core.client.currency.CurrencyServiceImpl
import dev.slne.surf.transaction.core.client.redis.RedisService
import dev.slne.surf.transaction.core.common.CoreTransactionSerializerModule
import dev.slne.surf.transaction.core.common.TransactionInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path

abstract class ClientTransactionalInstance : TransactionInstance() {
    val rabbitApi = ClientRabbitMQApi.create(
        "surf-transaction",
        dataPath,
        CoreTransactionSerializerModule.module
    )
    abstract val dataPath: Path
    abstract val scope: CoroutineScope

    override suspend fun load() {
        super.load()

        rabbitApi.freezeAndConnect()
        withContext(Dispatchers.IO) { RedisService.get().connect() }
        CurrencyServiceImpl.get().cacheCurrencies()
    }

    override suspend fun disable() {
        super.disable()

        CurrencyServiceImpl.get().disposeScope()
        withContext(Dispatchers.IO) { RedisService.get().disconnect() }
        rabbitApi.disconnect()
    }

    companion object {
        fun get() = TransactionInstance.get() as ClientTransactionalInstance
    }
}

val rabbitApi get() = ClientTransactionalInstance.get().rabbitApi