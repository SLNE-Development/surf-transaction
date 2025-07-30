/**
 * Provides extension utilities for performing currency transactions on an [OfflineCloudPlayer].
 *
 * All functions are `suspend` and delegate to [InternalTransactionUserBridge] for execution.
 * Amounts are primarily accepted as [BigDecimal] to avoid precision issues; `Double` overloads
 * are offered for convenience and convert internally via [BigDecimal.valueOf].
 *
 * ### Conventions
 * * `ignoreMinimum` flags allow bypassing configured minimum-balance requirements.
 * * Additional transaction metadata can be supplied via [TransactionData] (vararg or set).
 * * Each call returns a [TransactionResult] describing success, failure, and context data.
 */
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
 * Deposits [amount] into this player's account in the given [currency].
 *
 * @receiver the player whose balance will be credited
 * @param amount the amount to deposit; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param ignoreMinimum `true` to bypass minimum-balance validation
 * @param additionalData optional metadata attached to the transaction
 * @return a [TransactionResult] describing the outcome
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
 * Convenience overload delegating to [deposit] with `ignoreMinimum = false`.
 *
 * @receiver the player whose balance will be credited
 * @param amount the amount to deposit; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param additionalData optional metadata attached to the transaction
 * @return a [TransactionResult] describing the outcome
 */
suspend fun OfflineCloudPlayer.deposit(
    amount: BigDecimal,
    currency: Currency,
    vararg additionalData: TransactionData
) = deposit(amount, currency, false, *additionalData)

/**
 * Deposits [amount] (converted from `Double`) into this player's account.
 *
 * @receiver the player whose balance will be credited
 * @param amount the amount to deposit; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param ignoreMinimum `true` to bypass minimum-balance validation
 * @param additionalData optional metadata attached to the transaction
 * @return a [TransactionResult] describing the outcome
 */
suspend fun OfflineCloudPlayer.deposit(
    amount: Double,
    currency: Currency,
    ignoreMinimum: Boolean = false,
    vararg additionalData: TransactionData
) = deposit(amount.toBigDecimal(), currency, ignoreMinimum, *additionalData)

/**
 * Convenience overload delegating to the `Double`-based [deposit] with
 * `ignoreMinimum = false`.
 *
 * @receiver the player whose balance will be credited
 * @param amount the amount to deposit; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param additionalData optional metadata attached to the transaction
 * @return a [TransactionResult] describing the outcome
 */
suspend fun OfflineCloudPlayer.deposit(
    amount: Double,
    currency: Currency,
    vararg additionalData: TransactionData
) = deposit(BigDecimal.valueOf(amount), currency, *additionalData)
// endregion Deposit

// region Withdraw
/**
 * Withdraws [amount] from this player's account in the given [currency].
 *
 * @receiver the player whose balance will be debited
 * @param amount the amount to withdraw; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param ignoreMinimum `true` to bypass minimum-balance validation
 * @param additionalData optional metadata attached to the transaction
 * @return a [TransactionResult] describing the outcome
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
 * Convenience overload delegating to [withdraw] with `ignoreMinimum = false`.
 *
 * @receiver the player whose balance will be debited
 * @param amount the amount to withdraw; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param additionalData optional metadata attached to the transaction
 * @return a [TransactionResult] describing the outcome
 */
suspend fun OfflineCloudPlayer.withdraw(
    amount: BigDecimal,
    currency: Currency,
    vararg additionalData: TransactionData
) = withdraw(amount, currency, false, *additionalData)

/**
 * Withdraws [amount] (converted from `Double`) from this player's account.
 *
 * @receiver the player whose balance will be debited
 * @param amount the amount to withdraw; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param ignoreMinimum `true` to bypass minimum-balance validation
 * @param additionalData optional metadata attached to the transaction
 * @return a [TransactionResult] describing the outcome
 */
suspend fun OfflineCloudPlayer.withdraw(
    amount: Double,
    currency: Currency,
    ignoreMinimum: Boolean = false,
    vararg additionalData: TransactionData
) = withdraw(amount.toBigDecimal(), currency, ignoreMinimum, *additionalData)

/**
 * Convenience overload delegating to the `Double`-based [withdraw] with
 * `ignoreMinimum = false`.
 *
 * @receiver the player whose balance will be debited
 * @param amount the amount to withdraw; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param additionalData optional metadata attached to the transaction
 * @return a [TransactionResult] describing the outcome
 */
suspend fun OfflineCloudPlayer.withdraw(
    amount: Double,
    currency: Currency,
    vararg additionalData: TransactionData
) = withdraw(amount.toBigDecimal(), currency, *additionalData)
// endregion

// region Transfer
/**
 * Transfers [amount] from this player's account to [receiver].
 *
 * @receiver the player sending the funds
 * @param amount the amount to transfer; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param receiver the player receiving the funds
 * @param ignoreSenderMinimum `true` to bypass sender's minimum-balance validation
 * @param ignoreReceiverMinimum `true` to bypass receiver's minimum-balance validation
 * @param additionalSenderData optional metadata attached to the sender's leg
 * @param additionalReceiverData optional metadata attached to the receiver's leg
 * @return a [TransactionResult] describing the outcome
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
 * Convenience overload delegating to [transfer] with `ignoreSenderMinimum` and
 * `ignoreReceiverMinimum` defaulting to `false`.
 *
 * @receiver the player sending the funds
 * @param amount the amount to transfer; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param receiver the player receiving the funds
 * @param additionalSenderData optional metadata attached to the sender's leg
 * @param additionalReceiverData optional metadata attached to the receiver's leg
 * @return a [TransactionResult] describing the outcome
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
 * Transfers [amount] (converted from `Double`) from this player to [receiver].
 *
 * @receiver the player sending the funds
 * @param amount the amount to transfer; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param receiver the player receiving the funds
 * @param ignoreSenderMinimum `true` to bypass sender's minimum-balance validation
 * @param ignoreReceiverMinimum `true` to bypass receiver's minimum-balance validation
 * @param additionalSenderData optional metadata attached to the sender's leg
 * @param additionalReceiverData optional metadata attached to the receiver's leg
 * @return a [TransactionResult] describing the outcome
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
    amount.toBigDecimal(),
    currency,
    receiver,
    ignoreSenderMinimum,
    ignoreReceiverMinimum,
    additionalSenderData,
    additionalReceiverData
)

/**
 * Convenience overload delegating to the `Double`-based [transfer] with
 * `ignoreSenderMinimum` and `ignoreReceiverMinimum` defaulting to `false`.
 *
 * @receiver the player sending the funds
 * @param amount the amount to transfer; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param receiver the player receiving the funds
 * @param additionalSenderData optional metadata attached to the sender's leg
 * @param additionalReceiverData optional metadata attached to the receiver's leg
 * @return a [TransactionResult] describing the outcome
 */
suspend fun OfflineCloudPlayer.transfer(
    amount: Double,
    currency: Currency,
    receiver: OfflineCloudPlayer,
    additionalSenderData: ObjectSet<TransactionData> = objectSetOf(),
    additionalReceiverData: ObjectSet<TransactionData> = objectSetOf()
) = transfer(
    amount.toBigDecimal(),
    currency,
    receiver,
    additionalSenderData,
    additionalReceiverData
)
// endregion

// region Balance
/**
 * Retrieves this player's balance in [currency] as a [BigDecimal].
 *
 * @receiver the player whose balance is requested
 * @param currency the monetary unit of the returned balance
 * @return the current balance
 */
suspend fun OfflineCloudPlayer.balance(currency: Currency): BigDecimal =
    InternalTransactionUserBridge.instance.balanceDecimal(this, currency)

/**
 * Convenience wrapper returning the balance as `Double`.
 *
 * @receiver the player whose balance is requested
 * @param currency the monetary unit of the returned balance
 * @return the current balance
 */
@Deprecated(
    "Use balance(currency: Currency): BigDecimal instead for precision",
    ReplaceWith("balance(currency).toDouble()")
)
suspend fun OfflineCloudPlayer.balanceDouble(currency: Currency): Double =
    balance(currency).toDouble()
// endregion