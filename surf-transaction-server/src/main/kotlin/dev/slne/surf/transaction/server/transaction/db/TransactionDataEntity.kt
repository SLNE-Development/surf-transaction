package dev.slne.surf.transaction.server.transaction.db

import dev.slne.surf.transaction.api.transaction.data.TransactionData
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class TransactionDataEntity(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<TransactionDataEntity>(TransactionDataTable)

    var key by TransactionDataTable.key
    var value by TransactionDataTable.value

    var transaction by TransactionEntity referencedOn TransactionDataTable.transaction

    fun toApi() = TransactionData(
        key = key,
        value = value
    )
}