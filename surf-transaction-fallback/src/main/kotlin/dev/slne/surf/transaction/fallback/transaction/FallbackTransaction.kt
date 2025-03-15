package dev.slne.surf.transaction.fallback.transaction

import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.core.transaction.CoreTransaction
import dev.slne.surf.transaction.fallback.currency.FallbackCurrency
import dev.slne.surf.transaction.fallback.transaction.data.FallbackTransactionData
import dev.slne.surf.transaction.fallback.transaction.data.FallbackTransactionDataTable
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class FallbackTransaction(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<FallbackTransaction>(FallbackTransactionTable)

    var identifier by FallbackTransactionTable.identifier
    var sender by FallbackTransactionTable.sender
    var receiver by FallbackTransactionTable.receiver
    var amount by FallbackTransactionTable.amount
    var currency by FallbackCurrency referencedOn FallbackTransactionTable.currency

    val data by FallbackTransactionData referrersOn FallbackTransactionDataTable.transaction

    fun toTransaction() = CoreTransaction(
        identifier = identifier,
        sender = sender,
        receiver = receiver,
        amount = amount,
        currency = currency.toCurrency(),
    ).apply {
        data.addAll(this@FallbackTransaction.data.map {
            TransactionData(it.key, it.value)
        })
    }

    override fun toString(): String {
        return toTransaction().toString()
    }
}