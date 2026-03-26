package dev.slne.surf.transaction.microservice

import com.google.auto.service.AutoService
import dev.slne.surf.database.DatabaseApi
import dev.slne.surf.microservice.api.microservice.Microservice
import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import dev.slne.surf.transaction.core.common.CoreTransactionSerializerModule
import dev.slne.surf.transaction.core.common.TransactionInstance
import dev.slne.surf.transaction.microservice.db.CreateTables
import dev.slne.surf.transaction.microservice.handler.account.*
import dev.slne.surf.transaction.microservice.handler.currency.CreateCurrencyHandler
import dev.slne.surf.transaction.microservice.handler.currency.FindAllCurrenciesAndCreateDefaultCurrencyIfMissingHandler
import dev.slne.surf.transaction.microservice.handler.currency.MakeDefaultCurrencyHandler
import dev.slne.surf.transaction.microservice.handler.transaction.CreateTransactionHandler
import dev.slne.surf.transaction.microservice.handler.transaction.GetTransactionBalanceHandler
import dev.slne.surf.transaction.microservice.handler.transaction.TransferTransactionCreateHandler
import kotlin.io.path.Path

@AutoService(Microservice::class)
class TransactionMicroservice : Microservice() {
    val configPath = Path("config")

    private val databaseApi = DatabaseApi.create(configPath)
    private val rabbitApi =
        ServerRabbitMQApi.create("surf-transaction", configPath, CoreTransactionSerializerModule.module)

    override suspend fun onBootstrap(args: List<String>) {
        CreateTables.create()

        // Account
        rabbitApi.registerRequestHandler(AccountAddMemberHandler)
        rabbitApi.registerRequestHandler(CreateAccountHandler)
        rabbitApi.registerRequestHandler(DeleteAccountHandler)
        rabbitApi.registerRequestHandler(ExistsAccountByAccountNameHandler)
        rabbitApi.registerRequestHandler(FindAllAccountByOwnerHandler)
        rabbitApi.registerRequestHandler(FindAccountByAccountIdHandler)
        rabbitApi.registerRequestHandler(FindAccountByNameHandler)
        rabbitApi.registerRequestHandler(FindOrCreateDefaultAccountByPlayerUuidHandler)
        rabbitApi.registerRequestHandler(RemoveMemberFromAccountHandler)
        rabbitApi.registerRequestHandler(CompleteAccountNameSuggestionsHandler)

        // Currency
        rabbitApi.registerRequestHandler(CreateCurrencyHandler)
        rabbitApi.registerRequestHandler(FindAllCurrenciesAndCreateDefaultCurrencyIfMissingHandler)
        rabbitApi.registerRequestHandler(MakeDefaultCurrencyHandler)

        // Transaction
        rabbitApi.registerRequestHandler(GetTransactionBalanceHandler)
        rabbitApi.registerRequestHandler(CreateTransactionHandler)
        rabbitApi.registerRequestHandler(TransferTransactionCreateHandler)

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