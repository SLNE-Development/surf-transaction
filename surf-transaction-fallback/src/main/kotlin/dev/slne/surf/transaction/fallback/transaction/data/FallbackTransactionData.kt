package dev.slne.surf.transaction.fallback.transaction.data

import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

class FallbackTransactionData(id: EntityID<Long>) : LongEntity(id) {

    companion object : LongEntityClass<FallbackTransactionData>(FallbackTransactionDataTable)

    var transaction by FallbackTransactionDataTable.transaction
    var key by FallbackTransactionDataTable.key
    var value by FallbackTransactionDataTable.value

}
