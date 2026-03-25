package dev.slne.surf.transaction.core.common.transactional

import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.transactional.Transactional
import dev.slne.surf.transaction.core.common.transaction.TransactionServiceImpl
import java.math.BigDecimal
import java.util.*

class TransactionalImpl : Transactional {

    override suspend fun deposit(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult {
        return TransactionServiceImpl.Companion.get().deposit(
            account,
            initiator,
            amount,
            currency,
            ignoreMinimum,
            *additionalData
        )
    }

    override suspend fun withdraw(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult {
        return TransactionServiceImpl.Companion.get().withdraw(
            account,
            initiator,
            amount,
            currency,
            ignoreMinimum,
            *additionalData
        )
    }

    override suspend fun transfer(
        initiator: UUID,
        sender: Account,
        amount: BigDecimal,
        currency: Currency,
        receiver: Account,
        ignoreSenderMinimum: Boolean,
        ignoreReceiverMinimum: Boolean,
        additionalSenderData: Set<TransactionData>,
        additionalReceiverData: Set<TransactionData>
    ): TransactionResult {
        return TransactionServiceImpl.Companion.get().transfer(
            initiator,
            sender,
            amount,
            currency,
            receiver,
            ignoreSenderMinimum,
            ignoreReceiverMinimum,
            additionalSenderData,
            additionalReceiverData
        )
    }

    override suspend fun balance(
        account: Account,
        currency: Currency
    ): BigDecimal {
        return TransactionServiceImpl.Companion.get().balance(account, currency)
    }
}