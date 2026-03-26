package dev.slne.surf.transaction.microservice

import com.google.auto.service.AutoService
import dev.slne.surf.database.DatabaseApi
import dev.slne.surf.microservice.api.microservice.Microservice
import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import dev.slne.surf.transaction.core.common.CoreTransactionSerializerModule
import dev.slne.surf.transaction.core.common.TransactionInstance
import dev.slne.surf.transaction.microservice.db.CreateTables
import kotlin.io.path.Path

@AutoService(Microservice::class)
class TransactionMicroservice : Microservice() {
    val configPath = Path("config")

    private val databaseApi = DatabaseApi.create(configPath)
    private val rabbitApi = ServerRabbitMQApi.create("surf-transaction", configPath, CoreTransactionSerializerModule.module)

    override suspend fun onBootstrap(args: List<String>) {
        CreateTables.create()

        rabbitApi.freezeAndConnect()

        TransactionInstance.get().load()
        TransactionInstance.get().enable()
    }

    override suspend fun onDisable() {
        TransactionInstance.get().disable()
        rabbitApi.disconnect()
        databaseApi.shutdown()
    }
}