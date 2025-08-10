package dev.slne.surf.transaction.server.transaction.db

import dev.slne.surf.cloud.api.common.player.toOfflineCloudPlayer
import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongEntity
import dev.slne.surf.cloud.api.server.exposed.table.AuditableLongEntityClass
import dev.slne.surf.transaction.core.transaction.TransactionImpl
import dev.slne.surf.transaction.server.account.db.AccountEntity
import dev.slne.surf.transaction.server.currency.db.CurrencyEntity
import org.jetbrains.exposed.dao.id.EntityID

class TransactionEntity(id: EntityID<Long>) : AuditableLongEntity(id, TransactionTable) {
    companion object : AuditableLongEntityClass<TransactionEntity>(TransactionTable)

    var identifier by TransactionTable.identifier
    var initiator by TransactionTable.initiator
    var senderAccount by AccountEntity optionalReferencedOn TransactionTable.sender
    var receiverAccount by AccountEntity optionalReferencedOn TransactionTable.receiver
    var amount by TransactionTable.amount
    var currency by CurrencyEntity referencedOn TransactionTable.currency

    val data by TransactionDataEntity referrersOn TransactionDataTable.transaction

    fun toApi() = TransactionImpl(
        identifier = identifier,
        initiator = initiator?.toOfflineCloudPlayer(),
        senderAccountId = senderAccount?.accountId,
        receiverAccountId = receiverAccount?.accountId,
        amount = amount,
        currencyName = currency.name,
        data = data.map { it.toApi() }.toSet()
    )
}