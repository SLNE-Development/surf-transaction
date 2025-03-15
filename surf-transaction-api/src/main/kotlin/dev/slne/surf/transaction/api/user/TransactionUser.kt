package dev.slne.surf.transaction.api.user

import dev.slne.surf.surfapi.core.api.util.objectSetOf
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResultType
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.transactionApi
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.math.BigDecimal
import java.util.*

interface TransactionUser {

    /**
     * A unique identifier for the user
     */
    val uuid: UUID

    // region Deposit
    /**
     * Deposit the amount to the user's account
     *
     * @param amount The amount to deposit
     * @param currency The currency of the amount
     * @param ignoreMinimum Whether to ignore the minimum balance
     * @param additionalData Additional data for the transaction
     *
     * @return The result of the transaction
     */
    suspend fun deposit(
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean = false,
        vararg additionalData: TransactionData
    ): TransactionResultType

    /**
     * Deposit the amount to the user's account
     *
     * @param amount The amount to deposit
     * @param currency The currency of the amount
     * @param additionalData Additional data for the transaction
     *
     * @return The result of the transaction
     */
    suspend fun deposit(
        amount: BigDecimal,
        currency: Currency,
        vararg additionalData: TransactionData
    ) = deposit(amount, currency, false, *additionalData)

    /**
     * Deposit the amount to the user's account
     *
     * @param amount The amount to deposit
     * @param currency The currency of the amount
     * @param ignoreMinimum Whether to ignore the minimum balance
     * @param additionalData Additional data for the transaction
     *
     * @return The result of the transaction
     */
    suspend fun deposit(
        amount: Double,
        currency: Currency,
        ignoreMinimum: Boolean = false,
        vararg additionalData: TransactionData
    ) = deposit(BigDecimal.valueOf(amount), currency, ignoreMinimum, *additionalData)

    /**
     * Deposit the amount to the user's account
     *
     * @param amount The amount to deposit
     * @param currency The currency of the amount
     * @param additionalData Additional data for the transaction
     *
     * @return The result of the transaction
     */
    suspend fun deposit(
        amount: Double,
        currency: Currency,
        vararg additionalData: TransactionData
    ) = deposit(BigDecimal.valueOf(amount), currency, *additionalData)
    // endregion Deposit

    // region Withdraw
    /**
     * Withdraw the amount from the user's account
     *
     * @param amount The amount to withdraw
     * @param currency The currency of the amount
     * @param ignoreMinimum Whether to ignore the minimum balance
     * @param additionalData Additional data for the transaction
     *
     * @return The result of the transaction
     */
    suspend fun withdraw(
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean = false,
        vararg additionalData: TransactionData
    ): TransactionResultType

    /**
     * Withdraw the amount from the user's account
     *
     * @param amount The amount to withdraw
     * @param currency The currency of the amount
     * @param additionalData Additional data for the transaction
     *
     * @return The result of the transaction
     */
    suspend fun withdraw(
        amount: BigDecimal,
        currency: Currency,
        vararg additionalData: TransactionData
    ) = withdraw(amount, currency, false, *additionalData)

    /**
     * Withdraw the amount from the user's account
     *
     * @param amount The amount to withdraw
     * @param currency The currency of the amount
     * @param ignoreMinimum Whether to ignore the minimum balance
     * @param additionalData Additional data for the transaction
     *
     * @return The result of the transaction
     */
    suspend fun withdraw(
        amount: Double,
        currency: Currency,
        ignoreMinimum: Boolean = false,
        vararg additionalData: TransactionData
    ) = withdraw(BigDecimal.valueOf(amount), currency, ignoreMinimum, *additionalData)

    /**
     * Withdraw the amount from the user's account
     *
     * @param amount The amount to withdraw
     * @param currency The currency of the amount
     * @param additionalData Additional data for the transaction
     *
     * @return The result of the transaction
     */
    suspend fun withdraw(
        amount: Double,
        currency: Currency,
        vararg additionalData: TransactionData
    ) = withdraw(BigDecimal.valueOf(amount), currency, *additionalData)
    // endregion

    // region Transfer
    /**
     * Transfer the amount to the receiver's account
     *
     * @param amount The amount to transfer
     * @param currency The currency of the amount
     * @param receiver The receiver of the amount
     * @param ignoreSenderMinimum Whether to ignore the minimum balance of the sender
     * @param ignoreReceiverMinimum Whether to ignore the minimum balance of the receiver
     * @param additionalSenderData Additional data for the sender's transaction
     * @param additionalReceiverData Additional data for the receiver's transaction
     *
     * @return The result of the transaction
     */
    suspend fun transfer(
        amount: BigDecimal,
        currency: Currency,
        receiver: TransactionUser,
        ignoreSenderMinimum: Boolean = false,
        ignoreReceiverMinimum: Boolean = false,
        additionalSenderData: ObjectSet<TransactionData> = objectSetOf(),
        additionalReceiverData: ObjectSet<TransactionData> = objectSetOf()
    ): TransactionResultType

    /**
     * Transfer the amount to the receiver's account
     *
     * @param amount The amount to transfer
     * @param currency The currency of the amount
     * @param receiver The receiver of the amount
     * @param additionalSenderData Additional data for the sender's transaction
     * @param additionalReceiverData Additional data for the receiver's transaction
     *
     * @return The result of the transaction
     */
    suspend fun transfer(
        amount: BigDecimal,
        currency: Currency,
        receiver: TransactionUser,
        additionalSenderData: ObjectSet<TransactionData> = objectSetOf(),
        additionalReceiverData: ObjectSet<TransactionData> = objectSetOf()
    ) = transfer(
        amount,
        currency,
        receiver,
        ignoreSenderMinimum = false,
        ignoreReceiverMinimum = false,
        additionalSenderData,
        additionalReceiverData
    )

    /**
     * Transfer the amount to the receiver's account
     *
     * @param amount The amount to transfer
     * @param currency The currency of the amount
     * @param receiver The receiver of the amount
     * @param ignoreSenderMinimum Whether to ignore the minimum balance of the sender
     * @param ignoreReceiverMinimum Whether to ignore the minimum balance of the receiver
     * @param additionalSenderData Additional data for the sender's transaction
     * @param additionalReceiverData Additional data for the receiver's transaction
     *
     * @return The result of the transaction
     */
    suspend fun transfer(
        amount: Double,
        currency: Currency,
        receiver: TransactionUser,
        ignoreSenderMinimum: Boolean = false,
        ignoreReceiverMinimum: Boolean = false,
        additionalSenderData: ObjectSet<TransactionData> = objectSetOf(),
        additionalReceiverData: ObjectSet<TransactionData> = objectSetOf()
    ) = transfer(
        BigDecimal.valueOf(amount),
        currency,
        receiver,
        ignoreSenderMinimum,
        ignoreReceiverMinimum,
        additionalSenderData,
        additionalReceiverData
    )

    /**
     * Transfer the amount to the receiver's account
     *
     * @param amount The amount to transfer
     * @param currency The currency of the amount
     * @param receiver The receiver of the amount
     * @param additionalSenderData Additional data for the sender's transaction
     * @param additionalReceiverData Additional data for the receiver's transaction
     *
     * @return The result of the transaction
     */
    suspend fun transfer(
        amount: Double,
        currency: Currency,
        receiver: TransactionUser,
        additionalSenderData: ObjectSet<TransactionData> = objectSetOf(),
        additionalReceiverData: ObjectSet<TransactionData> = objectSetOf()
    ) = transfer(
        BigDecimal.valueOf(amount),
        currency,
        receiver,
        additionalSenderData,
        additionalReceiverData
    )
    // endregion

    // region Balance
    /**
     * Get the balance of the user's account
     *
     * @param currency The currency of the balance
     *
     * @return The balance of the user's account
     */
    suspend fun balanceDecimal(currency: Currency): BigDecimal

    /**
     * Get the balance of the user's account
     *
     * @param currency The currency of the balance
     *
     * @return The balance of the user's account
     */
    suspend fun balance(currency: Currency): Double = balanceDecimal(currency).toDouble()
    // endregion

    companion object {
        /**
         * Get the transaction user by the UUID
         *
         * @param uuid The UUID of the transaction user
         *
         * @return The transaction user
         */
        fun get(uuid: UUID) = transactionApi.getTransactionUser(uuid)
    }

}