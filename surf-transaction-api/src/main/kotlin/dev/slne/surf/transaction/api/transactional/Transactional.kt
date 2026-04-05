package dev.slne.surf.transaction.api.transactional

import dev.slne.surf.api.core.util.objectSetOf
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import org.jetbrains.annotations.ApiStatus
import java.math.BigDecimal
import java.util.UUID

/**
 * Defines transactional operations that can be performed on accounts.
 *
 * A [Transactional] implementation provides suspendable methods for modifying
 * account balances, including deposits, withdrawals, and transfers. All
 * operations return a [TransactionResult] describing the outcome.
 */
@OptIn(InternalTransactionApi::class)
@ApiStatus.NonExtendable
interface Transactional {

    /**
     * Deposits an amount into the given [account].
     *
     * @param account the target account receiving the funds
     * @param initiator the unique identifier of the entity initiating the transaction
     * @param amount the amount to deposit
     * @param currency the currency of the amount
     * @param ignoreMinimum whether the currency minimum amount should be ignored
     * @param additionalData optional additional transaction metadata
     *
     * @return the result of the transaction
     */
    suspend fun deposit(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean = false,
        vararg additionalData: TransactionData
    ): TransactionResult

    /**
     * Withdraws an amount from the given [account].
     *
     * @param account the source account from which funds are withdrawn
     * @param initiator the unique identifier of the entity initiating the transaction
     * @param amount the amount to withdraw
     * @param currency the currency of the amount
     * @param ignoreMinimum whether the currency minimum amount should be ignored
     * @param additionalData optional additional transaction metadata
     *
     * @return the result of the transaction
     */
    suspend fun withdraw(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean = false,
        vararg additionalData: TransactionData
    ): TransactionResult

    /**
     * Transfers an amount from one account to another.
     *
     * This operation withdraws the amount from the [sender] account and deposits
     * it into the [receiver] account as a single transactional operation.
     *
     * @param initiator the unique identifier of the entity initiating the transaction
     * @param sender the account sending the funds
     * @param amount the amount to transfer
     * @param currency the currency of the amount
     * @param receiver the account receiving the funds
     * @param ignoreSenderMinimum whether the sender's currency minimum should be ignored
     * @param ignoreReceiverMinimum whether the receiver's currency minimum should be ignored
     * @param additionalSenderData additional transaction metadata for the sender side
     * @param additionalReceiverData additional transaction metadata for the receiver side
     *
     * @return the result of the transaction
     */
    suspend fun transfer(
        initiator: UUID,
        sender: Account,
        amount: BigDecimal,
        currency: Currency,
        receiver: Account,
        ignoreSenderMinimum: Boolean = false,
        ignoreReceiverMinimum: Boolean = false,
        additionalSenderData: Set<TransactionData> = objectSetOf(),
        additionalReceiverData: Set<TransactionData> = objectSetOf()
    ): TransactionResult

    /**
     * Returns the current balance of the given [account] for the specified [currency].
     *
     * @param account the account whose balance should be queried
     * @param currency the currency of the balance
     *
     * @return the current balance
     */
    suspend fun balance(account: Account, currency: Currency): BigDecimal
}