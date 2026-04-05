package dev.slne.surf.transaction.microservice

import com.google.auto.service.AutoService
import dev.slne.surf.transaction.core.common.TransactionInstance

@AutoService(TransactionInstance::class)
class MicroserviceTransactionInstance : TransactionInstance() {
}