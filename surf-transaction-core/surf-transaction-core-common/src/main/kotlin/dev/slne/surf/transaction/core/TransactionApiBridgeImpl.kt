package dev.slne.surf.transaction.core

import com.google.auto.service.AutoService
import dev.slne.surf.surfapi.core.api.util.checkInstantiationByServiceLoader
import dev.slne.surf.transaction.api.InternalTransactionApiBridge
import org.springframework.context.ApplicationContext

@AutoService(InternalTransactionApiBridge::class)
class TransactionApiBridgeImpl : InternalTransactionApiBridge {
    init {
        checkInstantiationByServiceLoader()
    }

    override lateinit var context: ApplicationContext
}

val transactionApiBridgeImpl get() = InternalTransactionApiBridge.instance as TransactionApiBridgeImpl