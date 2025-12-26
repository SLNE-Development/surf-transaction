package dev.slne.surf.transaction.api.transaction

import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import org.jetbrains.annotations.Unmodifiable
import java.math.BigDecimal
import java.util.*

@OptIn(InternalTransactionApi::class)
interface Transaction {

    val identifier: UUID
    val initiator: UUID?
    val senderAccountId: UUID?
    val receiverAccountId: UUID?

    val currency: Currency
    val amount: BigDecimal
    val ignoreMinimumAmount: Boolean
    val data: @Unmodifiable Set<TransactionData>

    suspend fun receiverAccount(): Account?
    suspend fun senderAccount(): Account?
}
