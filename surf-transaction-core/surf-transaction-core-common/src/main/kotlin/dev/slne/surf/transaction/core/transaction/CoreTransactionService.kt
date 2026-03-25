package dev.slne.surf.transaction.core.transaction

import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.TransactionService
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import java.math.BigDecimal
import java.util.UUID

@InternalTransactionApi
interface CoreTransactionService : TransactionService {
    suspend fun deposit(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult

    suspend fun withdraw(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult

    suspend fun transfer(
        initiator: UUID,
        sender: Account,
        amount: BigDecimal,
        currency: Currency,
        receiver: Account,
        ignoreSenderMinimum: Boolean,
        ignoreReceiverMinimum: Boolean,
        additionalSenderData: Set<TransactionData>,
        additionalReceiverData: Set<TransactionData>
    ): TransactionResult

    suspend fun balance(account: Account, currency: Currency): BigDecimal

    companion object {
        @OptIn(InternalTransactionApi::class)
        fun get() = TransactionService.instance as CoreTransactionService
    }
}
