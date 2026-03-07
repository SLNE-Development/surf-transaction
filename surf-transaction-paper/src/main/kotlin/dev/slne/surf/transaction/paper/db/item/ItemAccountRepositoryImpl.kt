package dev.slne.surf.transaction.paper.db.item

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.eq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.sum
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.insertAndGetId
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.r2dbc.select
import dev.slne.surf.transaction.api.item.result.ItemTransactionResult
import dev.slne.surf.transaction.core.db.item.ItemTransactionTable
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.singleOrNull

class ItemAccountRepositoryImpl : ItemAccountRepository {

    suspend fun balance0(accountDBId: ULong): Int {
        return ItemTransactionTable
            .select(ItemTransactionTable.amount.sum())
            .where { ItemTransactionTable.receiver eq accountDBId }
            .map { it[ItemTransactionTable.amount.sum()] }
            .singleOrNull() ?: 0
    }

    suspend fun persistTransaction0(transaction: ItemTransactionImpl): ItemTransactionResult {
        val receiverDBId = transaction.receiverAccountId?.let { findAccountDBIdByAccountId(it) }
        val senderDBId = transaction.senderAccountId?.let { findAccountDBIdByAccountId(it) }
        val fingerprintDBId = findFingerprintDBIdByHash(transaction.fingerprintHash)
            ?: error("Fingerprint not found")

        val txId = ItemTransactionTable.insertAndGetId {
            it[identifier] = transaction.identifier
            it[initiator] = transaction.initiator
            it[sender] = senderDBId
            it[receiver] = receiverDBId
            it[amount] = transaction.amount
            it[fingerprint] = fingerprintDBId
        }.value

        // Bei Withdrawals (negativer amount) prüfen ob Balance >= 0 bleibt
        if (receiverDBId != null && transaction.amount < 0) {
            val balanceAfter = balance0(receiverDBId)
            if (balanceAfter < 0) {
                return ItemTransactionResult.InsufficientItems
            }
        }

        return ItemTransactionResult.Success(transaction.identifier)
    }
}