package dev.slne.surf.transaction.microservice

import com.google.auto.service.AutoService
import dev.slne.surf.microservice.api.microservice.Microservice
import dev.slne.surf.transaction.core.TransactionInstance
import dev.slne.surf.transaction.microservice.rabbit.TransactionRabbitHandler

@AutoService(Microservice::class)
class TransactionMicroservice : Microservice() {
    override suspend fun onBootstrap(args: List<String>) {
        TransactionInstance.get().load()
        TransactionInstance.get().enable()

        val rabbitApi = serverRabbitMQApi
        rabbitApi.registerRequestHandler(TransactionRabbitHandler())
        rabbitApi.freezeAndConnect()
    }

    override suspend fun onDisable() {
        TransactionInstance.get().disable()
    }
}
