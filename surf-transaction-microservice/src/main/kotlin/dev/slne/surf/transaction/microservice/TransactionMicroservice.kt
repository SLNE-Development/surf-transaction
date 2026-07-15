package dev.slne.surf.transaction.microservice

import com.google.auto.service.AutoService
import dev.slne.surf.api.core.util.runWithFixedDelay
import dev.slne.surf.database.DatabaseApi
import dev.slne.surf.microservice.api.microservice.Microservice
import dev.slne.surf.rabbitmq.api.ServerRabbitMQApi
import dev.slne.surf.transaction.core.common.CoreTransactionSerializerModule
import dev.slne.surf.transaction.core.common.TransactionInstance
import dev.slne.surf.transaction.core.common.rpc.TransactionRpcService
import dev.slne.surf.transaction.microservice.db.CreateTables
import dev.slne.surf.transaction.microservice.db.transaction.TransactionRepository
import dev.slne.surf.transaction.microservice.handler.account.*
import dev.slne.surf.transaction.microservice.handler.currency.CreateCurrencyHandler
import dev.slne.surf.transaction.microservice.handler.currency.FindAllCurrenciesAndCreateDefaultCurrencyIfMissingHandler
import dev.slne.surf.transaction.microservice.handler.currency.MakeDefaultCurrencyHandler
import dev.slne.surf.transaction.microservice.handler.transaction.*
import dev.slne.surf.transaction.microservice.rpc.TransactionRpcServiceImpl
import kotlinx.coroutines.*
import kotlin.io.path.Path
import kotlin.time.Duration.Companion.seconds

@AutoService(Microservice::class)
class TransactionMicroservice : Microservice() {
    override val dataPath = Path("config")

    private val databaseApi = DatabaseApi.create(dataPath)
    private val rabbitApi = ServerRabbitMQApi.create(
        "surf-transaction",
        dataPath,
        CoreTransactionSerializerModule.module
    )
    private val expirationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var expirationJob: Job? = null

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
        rabbitApi.registerRpcService<TransactionRpcService>(TransactionRpcServiceImpl)

        rabbitApi.freezeAndConnect()

        TransactionRepository.expireTransactions()
        expirationJob =
            expirationScope.runWithFixedDelay(90.seconds, taskName = "Transaction expiration") {
                TransactionRepository.expireTransactions()
            }

        TransactionInstance.INSTANCE.load()
        TransactionInstance.INSTANCE.enable()
    }

    override suspend fun onDisable() {
        expirationJob?.cancelAndJoin()
        TransactionInstance.INSTANCE.disable()
        rabbitApi.disconnect()
        databaseApi.shutdown()
    }
}
