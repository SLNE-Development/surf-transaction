package dev.slne.surf.transaction.paper.db.item

import dev.slne.surf.transaction.api.item.result.ItemTransactionResult
import java.util.UUID

interface ItemAccountRepository {
    suspend fun findOrCreateFingerprint(
        hash: String,
        itemType: String,
        componentData: String?,
        serializedTemplate: ByteArray,
        dataVersion: Int
    ): ULong

    suspend fun findOrCreateAccount(
        ownerUuid: UUID,
        fingerprintId: ULong,
        name: String?
    ): ItemAccountImpl

    suspend fun findAccountByAccountId(accountId: UUID): ItemAccountImpl?
    suspend fun findAccountsByOwner(ownerUuid: UUID): List<ItemAccountImpl>

    suspend fun persistTransaction(transaction: ItemTransactionImpl): ItemTransactionResult

    suspend fun transfer(
        senderTx: ItemTransactionImpl,
        receiverTx: ItemTransactionImpl
    ): ItemTransactionResult

    suspend fun balance(accountId: UUID): Int

    companion object : ItemAccountRepository by ItemAccountRepositoryImpl()
}