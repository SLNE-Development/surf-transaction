package dev.slne.surf.transaction.microservice.db.transaction.table

import dev.slne.surf.database.columns.nativeUuid
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.ReferenceOption
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.isNotNull
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.neq
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.or
import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.javatime.timestamp
import dev.slne.surf.database.table.AuditableLongIdTable
import dev.slne.surf.transaction.api.transaction.TransactionState
import dev.slne.surf.transaction.microservice.db.account.AccountTable
import dev.slne.surf.transaction.microservice.db.currency.CurrencyTable

object TransactionTable : AuditableLongIdTable("transactions") {
    val identifier = nativeUuid("identifier")
        .uniqueIndex()

    val initiator = nativeUuid("initiator_id")
        .nullable()

    val sender = optReference(
        "sender_account", AccountTable,
        onUpdate = ReferenceOption.CASCADE,
        onDelete = ReferenceOption.CASCADE
    ).clientDefault { null }

    val receiver = optReference(
        "receiver_account", AccountTable,
        onUpdate = ReferenceOption.CASCADE,
        onDelete = ReferenceOption.CASCADE
    ).clientDefault { null }

    val currency = reference(
        "currency", CurrencyTable,
        onUpdate = ReferenceOption.CASCADE,
        onDelete = ReferenceOption.CASCADE
    )

    val amount = decimal("amount", 20, 10)

    val ignoreMinimumAmount = bool("ignore_minimum_amount")
        .default(false)

    val state = enumerationByName<TransactionState>("state", 16)
        .default(TransactionState.COMMITTED)

    val expiresAt = timestamp("expires_at")
        .nullable()

    val operationId = nativeUuid("operation_id")
        .nullable()

    init {
        index(false, receiver, currency)
        index(false, state, expiresAt)
        index(false, operationId, state, expiresAt)
        check("transactions_pending_has_expiration") {
            (state neq TransactionState.PENDING) or expiresAt.isNotNull()
        }
        check("transactions_pending_has_operation") {
            (state neq TransactionState.PENDING) or operationId.isNotNull()
        }
    }
}