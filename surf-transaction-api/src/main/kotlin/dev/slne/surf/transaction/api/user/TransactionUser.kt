@file:OptIn(InternalTransactionApi::class)

package dev.slne.surf.transaction.api.user

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.surfapi.core.api.util.objectSetOf
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.math.BigDecimal


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
suspend fun OfflineCloudPlayer.deposit(
    amount: BigDecimal,
    currency: Currency,
    ignoreMinimum: Boolean = false,
    vararg additionalData: TransactionData
): TransactionResult = InternalTransactionUserBridge.instance.deposit(
    this,
    amount,
    currency,
    ignoreMinimum,
    *additionalData
)

/**
 * Deposit the amount to the user's account
 *
 * @param amount The amount to deposit
 * @param currency The currency of the amount
 * @param additionalData Additional data for the transaction
 *
 * @return The result of the transaction
 */
suspend fun OfflineCloudPlayer.deposit(
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
suspend fun OfflineCloudPlayer.deposit(
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
suspend fun OfflineCloudPlayer.deposit(
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
suspend fun OfflineCloudPlayer.withdraw(
    amount: BigDecimal,
    currency: Currency,
    ignoreMinimum: Boolean = false,
    vararg additionalData: TransactionData
): TransactionResult = InternalTransactionUserBridge.instance.withdraw(
    this,
    amount,
    currency,
    ignoreMinimum,
    *additionalData
)

/**
 * Withdraw the amount from the user's account
 *
 * @param amount The amount to withdraw
 * @param currency The currency of the amount
 * @param additionalData Additional data for the transaction
 *
 * @return The result of the transaction
 */
suspend fun OfflineCloudPlayer.withdraw(
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
suspend fun OfflineCloudPlayer.withdraw(
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
suspend fun OfflineCloudPlayer.withdraw(
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
suspend fun OfflineCloudPlayer.transfer(
    amount: BigDecimal,
    currency: Currency,
    receiver: OfflineCloudPlayer,
    ignoreSenderMinimum: Boolean = false,
    ignoreReceiverMinimum: Boolean = false,
    additionalSenderData: ObjectSet<TransactionData> = objectSetOf(),
    additionalReceiverData: ObjectSet<TransactionData> = objectSetOf()
): TransactionResult = InternalTransactionUserBridge.instance.transfer(
    this,
    amount,
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
suspend fun OfflineCloudPlayer.transfer(
    amount: BigDecimal,
    currency: Currency,
    receiver: OfflineCloudPlayer,
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
suspend fun OfflineCloudPlayer.transfer(
    amount: Double,
    currency: Currency,
    receiver: OfflineCloudPlayer,
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
suspend fun OfflineCloudPlayer.transfer(
    amount: Double,
    currency: Currency,
    receiver: OfflineCloudPlayer,
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
suspend fun OfflineCloudPlayer.balanceDecimal(currency: Currency): BigDecimal =
    InternalTransactionUserBridge.instance.balanceDecimal(this, currency)

/**
 * Get the balance of the user's account
 *
 * @param currency The currency of the balance
 *
 * @return The balance of the user's account
 */
suspend fun OfflineCloudPlayer.balance(currency: Currency): Double =
    balanceDecimal(currency).toDouble()
// endregion