package dev.slne.surf.transaction.core.common

import dev.slne.surf.transaction.api.transaction.Transaction
import dev.slne.surf.transaction.core.common.transaction.TransactionImpl
import kotlinx.serialization.modules.SerializersModule

object CoreTransactionSerializerModule {
    val module = SerializersModule {
        polymorphic(Transaction::class, TransactionImpl::class, TransactionImpl.serializer())
    }
}