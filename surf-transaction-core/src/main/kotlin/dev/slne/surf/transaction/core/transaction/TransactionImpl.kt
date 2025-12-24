package dev.slne.surf.transaction.core.transaction

import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.Transaction
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import org.jetbrains.annotations.Unmodifiable
import java.math.BigDecimal
import java.util.*

class TransactionImpl(
    override val identifier: UUID,
    override val initiator: UUID?,
    override val senderAccountId: UUID?,
    override val receiverAccountId: UUID?,
    val currencyName: String,
    override val amount: BigDecimal,
    override val data: @Unmodifiable Set<TransactionData>,
    override val ignoreMinimumAmount: Boolean = false
) : Transaction {
    override val currency: Currency
        get() = Currency.byName(currencyName)
            ?: error("Currency with name '$currencyName' not found. Ensure the currency is registered in the system.")

    override suspend fun senderAccount() = senderAccountId?.let { Account.byId(it) }
    override suspend fun receiverAccount() = receiverAccountId?.let { Account.byId(it) }
}