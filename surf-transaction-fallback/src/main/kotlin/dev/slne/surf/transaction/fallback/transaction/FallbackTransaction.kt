package dev.slne.surf.transaction.fallback.transaction

import dev.slne.surf.transaction.core.transaction.CoreTransaction
import dev.slne.surf.transaction.fallback.currency.FallbackCurrency
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
    var extra by FallbackTransactionTable.extra

    fun toTransaction() = CoreTransaction(
        identifier = identifier,
        sender = sender,
        receiver = receiver,
        amount = amount,
        currency = currency.toCurrency(),
    ).apply { extra.data.forEach { addData(it) } }
}