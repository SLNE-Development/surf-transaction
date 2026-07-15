package dev.slne.surf.transaction.core.common.transaction

import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.Transaction
import dev.slne.surf.transaction.api.transaction.TransactionService
import dev.slne.surf.transaction.api.transaction.TransactionState
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.jetbrains.annotations.Unmodifiable
import java.math.BigDecimal
import java.time.Instant
import java.util.*

@Serializable
data class TransactionImpl(
    override val identifier: @Contextual UUID,
    override val initiator: @Contextual UUID?,
    override val senderAccountId: @Contextual UUID?,
    override val receiverAccountId: @Contextual UUID?,
    val currencyName: String,
    override val amount: @Contextual BigDecimal,
    override val data: @Unmodifiable Set<TransactionData>,
    override val ignoreMinimumAmount: Boolean = false,
    override val state: TransactionState = TransactionState.COMMITTED,
    override val expiresAt: @Contextual Instant? = null
) : Transaction {
    override val currency: Currency
        get() = Currency.byName(currencyName)
            ?: error("Currency with name '$currencyName' not found. Ensure the currency is registered in the system.")

    override suspend fun senderAccount() = senderAccountId?.let { Account.byId(it) }
    override suspend fun receiverAccount() = receiverAccountId?.let { Account.byId(it) }

    override suspend fun commit() = TransactionService.commit(identifier)
    override suspend fun rollback() = TransactionService.rollback(identifier)
    override suspend fun refresh() = TransactionService.find(identifier)
}
