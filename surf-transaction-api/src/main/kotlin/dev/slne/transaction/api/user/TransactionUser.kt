package dev.slne.transaction.api.user

import dev.slne.transaction.api.currency.Currency
import dev.slne.transaction.api.transaction.TransactionResult
import java.math.BigDecimal
import java.util.*

interface TransactionUser {

    val uuid: UUID

    /**
     * Deposit the amount to the user's account
     *
     * @param amount The amount to deposit
     * @param currency The currency of the amount
     *
     * @return The result of the transaction
     */
    suspend fun deposit(amount: BigDecimal, currency: Currency): TransactionResult

    /**
     * Withdraw the amount from the user's account
     *
     * @param amount The amount to withdraw
     * @param currency The currency of the amount
     *
     * @return The result of the transaction
     */
    suspend fun withdraw(amount: BigDecimal, currency: Currency): TransactionResult

    /**
     * Transfer the amount to the receiver's account
     *
     * @param amount The amount to transfer
     * @param currency The currency of the amount
     * @param receiver The receiver of the amount
     *
     * @return The result of the transaction
     */
    suspend fun transfer(amount: BigDecimal, currency: Currency, receiver: TransactionUser): TransactionResult

    /**
     * Get the balance of the user's account
     *
     * @param currency The currency of the balance
     *
     * @return The balance of the user's account
     */
    suspend fun balanceDecimal(currency: Currency): BigDecimal

    /**
     * Deposit the amount to the user's account
     *
     * @param amount The amount to deposit
     * @param currency The currency of the amount
     *
     * @return The result of the transaction
     */
    suspend fun deposit(amount: Double, currency: Currency) = deposit(BigDecimal.valueOf(amount), currency)

    /**
     * Withdraw the amount from the user's account
     *
     * @param amount The amount to withdraw
     * @param currency The currency of the amount
     *
     * @return The result of the transaction
     */
    suspend fun withdraw(amount: Double, currency: Currency) = withdraw(BigDecimal.valueOf(amount), currency)

    /**
     * Transfer the amount to the receiver's account
     *
     * @param amount The amount to transfer
     * @param currency The currency of the amount
     * @param receiver The receiver of the amount
     *
     * @return The result of the transaction
     */
    suspend fun transfer(amount: Double, currency: Currency, receiver: TransactionUser) = transfer(BigDecimal.valueOf(amount), currency, receiver)

    /**
     * Get the balance of the user's account
     *
     * @param currency The currency of the balance
     *
     * @return The balance of the user's account
     */
    suspend fun balance(currency: Currency): Double = balanceDecimal(currency).toDouble()

    companion object {
        /**
         * Get the transaction user by the UUID
         *
         * @param uuid The UUID of the transaction user
         *
         * @return The transaction user
         */
        fun get(uuid: UUID) = transactionUserManager[uuid]
    }

}