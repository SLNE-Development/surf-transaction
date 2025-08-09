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
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.account.AccountDeleteResult
import dev.slne.surf.transaction.api.account.InternalAccountBridge
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.math.BigDecimal

// region Accounts
/**
 * Retrieves the default account for this player.
 *
 * @receiver the player whose default account is requested
 * @return the default [Account] associated with this player
 */
suspend fun OfflineCloudPlayer.defaultAccount() =
    InternalAccountBridge.instance.getDefaultAccount(this)

/**
 * Retrieves all accounts associated with this player.
 *
 * @receiver the player whose accounts are requested
 * @return a set of [Account]s owned by this player
 */
suspend fun OfflineCloudPlayer.accounts() =
    InternalAccountBridge.instance.getAccounts(this)

/**
 * Creates a new account for this player with the specified [name].
 *
 * @receiver the player for whom the account will be created
 * @param name the name of the new account; must be non-empty
 * @return the newly created [Account]
 */
suspend fun OfflineCloudPlayer.createAccount(
    name: String
) = InternalAccountBridge.instance.createAccount(this, name)

/**
 * Retrieves the account with the specified [accountName] for this player.
 *
 * @receiver the player whose account is requested
 * @param accountName the name of the account to retrieve; must be non-empty
 * @return the [Account] matching [accountName], or `null` if not found
 */
suspend fun OfflineCloudPlayer.accountByName(accountName: String) =
    InternalAccountBridge.instance.getAccountByName(accountName)

/**
 * Deletes the specified [account] from this player's accounts.
 *
 * @receiver the player whose account will be deleted
 * @param account the account to delete; must be non-null
 * @return an [AccountDeleteResult] indicating success or failure
 */
suspend fun OfflineCloudPlayer.deleteAccount(
    account: Account
) = InternalAccountBridge.instance.deleteAccount(account)
// endregion

// region Deposit
/**
 * Deposits [amount] into this player's account in the given [currency].
 *
 * @receiver the player whose balance will be credited
 * @param account the account to deposit into; must be non-null
 * @param initiator the player initiating the deposit; defaults to `this`
 * @param amount the amount to deposit; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param ignoreMinimum `true` to bypass minimum-balance validation
 * @param additionalData optional metadata attached to the transaction
 * @return a [TransactionResult] describing the outcome
 */
suspend fun OfflineCloudPlayer.deposit(
    account: Account,
    initiator: OfflineCloudPlayer = this,
    amount: BigDecimal,
    currency: Currency,
    ignoreMinimum: Boolean = false,
    vararg additionalData: TransactionData
): TransactionResult = InternalTransactionUserBridge.instance.deposit(
    account,
    initiator,
    amount,
    currency,
    ignoreMinimum,
    *additionalData
)

/**
 * Convenience overload delegating to [deposit] with `ignoreMinimum = false`.
 *
 * @receiver the player whose balance will be credited
 * @param initiator the player initiating the deposit; defaults to `this`
 * @param amount the amount to deposit; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param additionalData optional metadata attached to the transaction
 * @return a [TransactionResult] describing the outcome
 */
suspend fun OfflineCloudPlayer.deposit(
    initiator: OfflineCloudPlayer = this,
    amount: BigDecimal,
    currency: Currency,
    vararg additionalData: TransactionData
) = deposit(defaultAccount(), initiator, amount, currency, false, *additionalData)

/**
 * Convenience overload delegating to [deposit] with `ignoreMinimum = false`.
 *
 * @receiver the player whose balance will be credited
 * @param account the account to deposit into; must be non-null
 * @param initiator the player initiating the deposit; defaults to `this`
 * @param amount the amount to deposit; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param additionalData optional metadata attached to the transaction
 * @return a [TransactionResult] describing the outcome
 */
suspend fun OfflineCloudPlayer.deposit(
    account: Account,
    initiator: OfflineCloudPlayer = this,
    amount: BigDecimal,
    currency: Currency,
    vararg additionalData: TransactionData
) = deposit(account, initiator, amount, currency, false, *additionalData)

/**
 * Convenience overload delegating to [deposit] with `ignoreMinimum = false`.
 *
 * @receiver the player whose balance will be credited
 * @param initiator the player initiating the deposit; defaults to `this`
 * @param amount the amount to deposit; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param ignoreMinimum `true` to bypass minimum-balance validation
 * @param additionalData optional metadata attached to the transaction
 * @return a [TransactionResult] describing the outcome
 */
suspend fun OfflineCloudPlayer.deposit(
    initiator: OfflineCloudPlayer = this,
    amount: BigDecimal,
    currency: Currency,
    ignoreMinimum: Boolean = false,
    vararg additionalData: TransactionData
) = deposit(defaultAccount(), initiator, amount, currency, ignoreMinimum, *additionalData)

/**
 * Deposits [amount] (converted from `Double`) into this player's account.
 *
 * @receiver the player whose balance will be credited
 * @param account the account to deposit into; must be non-null
 * @param initiator the player initiating the deposit; defaults to `this`
 * @param amount the amount to deposit; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param ignoreMinimum `true` to bypass minimum-balance validation
 * @param additionalData optional metadata attached to the transaction
 * @return a [TransactionResult] describing the outcome
 */
suspend fun OfflineCloudPlayer.deposit(
    account: Account,
    initiator: OfflineCloudPlayer = this,
    amount: Double,
    currency: Currency,
    ignoreMinimum: Boolean = false,
    vararg additionalData: TransactionData
) = deposit(account, initiator, amount.toBigDecimal(), currency, ignoreMinimum, *additionalData)

/**
 * Convenience overload delegating to the `Double`-based [deposit] with
 * `ignoreMinimum = false`.
 *
 * @receiver the player whose balance will be credited
 * @param initiator the player initiating the deposit; defaults to `this`
 * @param amount the amount to deposit; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param additionalData optional metadata attached to the transaction
 * @return a [TransactionResult] describing the outcome
 */
suspend fun OfflineCloudPlayer.deposit(
    initiator: OfflineCloudPlayer = this,
    amount: Double,
    currency: Currency,
    ignoreMinimum: Boolean = false,
    vararg additionalData: TransactionData
) = deposit(
    defaultAccount(),
    initiator,
    amount.toBigDecimal(),
    currency,
    ignoreMinimum,
    *additionalData
)

/**
 * Convenience overload delegating to the `Double`-based [deposit] with
 * `ignoreMinimum = false`.
 *
 * @receiver the player whose balance will be credited
 * @param account the account to deposit into; must be non-null
 * @param initiator the player initiating the deposit; defaults to `this`
 * @param amount the amount to deposit; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param additionalData optional metadata attached to the transaction
 * @return a [TransactionResult] describing the outcome
 */
suspend fun OfflineCloudPlayer.deposit(
    account: Account,
    initiator: OfflineCloudPlayer = this,
    amount: Double,
    currency: Currency,
    vararg additionalData: TransactionData
) = deposit(account, initiator, BigDecimal.valueOf(amount), currency, *additionalData)

/**
 * Convenience overload delegating to the `Double`-based [deposit] with
 * `ignoreMinimum = false`.
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
): TransactionResult =
    deposit(defaultAccount(), this, amount, currency, ignoreMinimum, *additionalData)
// endregion Deposit

// region Withdraw
/**
 * Withdraws [amount] from this player's account in the given [currency].
 *
 * @receiver the player whose balance will be debited
 * @param account the account to withdraw from; must be non-null
 * @param initiator the player initiating the withdrawal; defaults to `this`
 * @param amount the amount to withdraw; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param ignoreMinimum `true` to bypass minimum-balance validation
 * @param additionalData optional metadata attached to the transaction
 * @return a [TransactionResult] describing the outcome
 */
suspend fun OfflineCloudPlayer.withdraw(
    account: Account,
    initiator: OfflineCloudPlayer = this,
    amount: BigDecimal,
    currency: Currency,
    ignoreMinimum: Boolean = false,
    vararg additionalData: TransactionData
): TransactionResult = InternalTransactionUserBridge.instance.withdraw(
    account,
    initiator,
    amount,
    currency,
    ignoreMinimum,
    *additionalData
)

/**
 * Convenience overload delegating to [withdraw] with `ignoreMinimum = false`.
 *
 * @receiver the player whose balance will be debited
 * @param initiator the player initiating the withdrawal; defaults to `this`
 * @param amount the amount to withdraw; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param additionalData optional metadata attached to the transaction
 * @return a [TransactionResult] describing the outcome
 */
suspend fun OfflineCloudPlayer.withdraw(
    initiator: OfflineCloudPlayer = this,
    amount: BigDecimal,
    currency: Currency,
    vararg additionalData: TransactionData
) = withdraw(defaultAccount(), initiator, amount, currency, false, *additionalData)

/**
 * Convenience overload delegating to [withdraw] with `ignoreMinimum = false`.
 *
 * @receiver the player whose balance will be debited
 * @param account the account to withdraw from; must be non-null
 * @param initiator the player initiating the withdrawal; defaults to `this`
 * @param amount the amount to withdraw; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param additionalData optional metadata attached to the transaction
 * @return a [TransactionResult] describing the outcome
 */
suspend fun OfflineCloudPlayer.withdraw(
    account: Account,
    initiator: OfflineCloudPlayer = this,
    amount: BigDecimal,
    currency: Currency,
    vararg additionalData: TransactionData
) = withdraw(account, initiator, amount, currency, false, *additionalData)

/**
 * Convenience overload delegating to [withdraw] with `ignoreMinimum = false`.
 *
 * @receiver the player whose balance will be debited
 * @param initiator the player initiating the withdrawal; defaults to `this`
 * @param amount the amount to withdraw; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param ignoreMinimum `true` to bypass minimum-balance validation
 * @param additionalData optional metadata attached to the transaction
 * @return a [TransactionResult] describing the outcome
 */
suspend fun OfflineCloudPlayer.withdraw(
    initiator: OfflineCloudPlayer = this,
    amount: BigDecimal,
    currency: Currency,
    ignoreMinimum: Boolean = false,
    vararg additionalData: TransactionData
) = withdraw(defaultAccount(), initiator, amount, currency, ignoreMinimum, *additionalData)

/**
 * Withdraws [amount] (converted from `Double`) from this player's account.
 *
 * @receiver the player whose balance will be debited
 * @param account the account to withdraw from; must be non-null
 * @param initiator the player initiating the withdrawal; defaults to `this`
 * @param amount the amount to withdraw; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param ignoreMinimum `true` to bypass minimum-balance validation
 * @param additionalData optional metadata attached to the transaction
 * @return a [TransactionResult] describing the outcome
 */
suspend fun OfflineCloudPlayer.withdraw(
    account: Account,
    initiator: OfflineCloudPlayer = this,
    amount: Double,
    currency: Currency,
    ignoreMinimum: Boolean = false,
    vararg additionalData: TransactionData
) = withdraw(account, initiator, amount.toBigDecimal(), currency, ignoreMinimum, *additionalData)

/**
 * Convenience overload delegating to the `Double`-based [withdraw] with
 * `ignoreMinimum = false`.
 *
 * @receiver the player whose balance will be debited
 * @param initiator the player initiating the withdrawal; defaults to `this`
 * @param amount the amount to withdraw; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param additionalData optional metadata attached to the transaction
 * @return a [TransactionResult] describing the outcome
 */
suspend fun OfflineCloudPlayer.withdraw(
    initiator: OfflineCloudPlayer = this,
    amount: Double,
    currency: Currency,
    vararg additionalData: TransactionData
) = withdraw(
    defaultAccount(),
    initiator,
    amount.toBigDecimal(),
    currency,
    false,
    *additionalData
)

/**
 * Convenience overload delegating to the `Double`-based [withdraw] with
 * `ignoreMinimum = false`.
 *
 * @receiver the player whose balance will be debited
 * @param initiator the player initiating the withdrawal; defaults to `this`
 * @param account the account to withdraw from; must be non-null
 * @param amount the amount to withdraw; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param additionalData optional metadata attached to the transaction
 * @return a [TransactionResult] describing the outcome
 */
suspend fun OfflineCloudPlayer.withdraw(
    initiator: OfflineCloudPlayer = this,
    account: Account,
    amount: Double,
    currency: Currency,
    vararg additionalData: TransactionData
) = withdraw(account, initiator, amount.toBigDecimal(), currency, *additionalData)

/**
 * Convenience overload delegating to the `Double`-based [withdraw] with
 * `ignoreMinimum = false`.
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
): TransactionResult = withdraw(
    defaultAccount(),
    this,
    amount, currency,
    ignoreMinimum,
    *additionalData
)

/**
 * Convenience overload delegating to the `Double`-based [withdraw] with
 * `ignoreMinimum = false`.
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
): TransactionResult = withdraw(
    defaultAccount(),
    this,
    amount.toBigDecimal(),
    currency,
    ignoreMinimum,
    *additionalData
)

// endregion

// region Transfer
/**
 * Transfers [amount] from this player's account to [receiver].
 *
 * @receiver the player sending the funds
 * @param initiator the player initiating the transfer; defaults to `this`
 * @param sender the account sending the funds; must be non-null
 * @param amount the amount to transfer; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param receiver the account receiving the funds
 * @param ignoreSenderMinimum `true` to bypass sender's minimum-balance validation
 * @param ignoreReceiverMinimum `true` to bypass receiver's minimum-balance validation
 * @param additionalSenderData optional metadata attached to the sender's leg
 * @param additionalReceiverData optional metadata attached to the receiver's leg
 * @return a [TransactionResult] describing the outcome
 */
suspend fun OfflineCloudPlayer.transfer(
    initiator: OfflineCloudPlayer = this,
    sender: Account,
    amount: BigDecimal,
    currency: Currency,
    receiver: Account,
    ignoreSenderMinimum: Boolean = false,
    ignoreReceiverMinimum: Boolean = false,
    additionalSenderData: ObjectSet<TransactionData> = objectSetOf(),
    additionalReceiverData: ObjectSet<TransactionData> = objectSetOf()
): TransactionResult = InternalTransactionUserBridge.instance.transfer(
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

/**
 * Convenience overload delegating to [transfer] with `ignoreSenderMinimum` and
 * `ignoreReceiverMinimum` defaulting to `false`.
 *
 * @receiver the player sending the funds
 * @param initiator the player initiating the transfer; defaults to `this`
 * @param sender the account sending the funds; must be non-null
 * @param amount the amount to transfer; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param receiver the player receiving the funds
 * @param additionalSenderData optional metadata attached to the sender's leg
 * @param additionalReceiverData optional metadata attached to the receiver's leg
 * @return a [TransactionResult] describing the outcome
 */
suspend fun OfflineCloudPlayer.transfer(
    initiator: OfflineCloudPlayer = this,
    sender: OfflineCloudPlayer = this,
    amount: BigDecimal,
    currency: Currency,
    receiver: OfflineCloudPlayer,
    additionalSenderData: ObjectSet<TransactionData> = objectSetOf(),
    additionalReceiverData: ObjectSet<TransactionData> = objectSetOf()
) = transfer(
    initiator,
    sender.defaultAccount(),
    amount,
    currency,
    receiver.defaultAccount(),
    ignoreSenderMinimum = false,
    ignoreReceiverMinimum = false,
    additionalSenderData,
    additionalReceiverData
)

/**
 * Convenience overload delegating to [transfer] with `ignoreSenderMinimum` and
 * `ignoreReceiverMinimum` defaulting to `false`.
 *
 * @receiver the player sending the funds
 * @param initiator the player initiating the transfer; defaults to `this`
 * @param sender the account sending the funds; must be non-null
 * @param amount the amount to transfer; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param receiver the player receiving the funds
 * @param additionalSenderData optional metadata attached to the sender's leg
 * @param additionalReceiverData optional metadata attached to the receiver's leg
 * @return a [TransactionResult] describing the outcome
 */
suspend fun OfflineCloudPlayer.transfer(
    initiator: OfflineCloudPlayer = this,
    sender: Account,
    amount: BigDecimal,
    currency: Currency,
    receiver: Account,
    additionalSenderData: ObjectSet<TransactionData> = objectSetOf(),
    additionalReceiverData: ObjectSet<TransactionData> = objectSetOf()
) = transfer(
    initiator,
    sender,
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
 * @param initiator the player initiating the transfer; defaults to `this`
 * @param sender the account sending the funds; must be non-null
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
    initiator: OfflineCloudPlayer = this,
    sender: Account,
    amount: Double,
    currency: Currency,
    receiver: Account,
    ignoreSenderMinimum: Boolean = false,
    ignoreReceiverMinimum: Boolean = false,
    additionalSenderData: ObjectSet<TransactionData> = objectSetOf(),
    additionalReceiverData: ObjectSet<TransactionData> = objectSetOf()
) = transfer(
    initiator,
    sender,
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
 * @param initiator the player initiating the transfer; defaults to `this`
 * @param sender the account sending the funds; must be non-null
 * @param amount the amount to transfer; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param receiver the player receiving the funds
 * @param additionalSenderData optional metadata attached to the sender's leg
 * @param additionalReceiverData optional metadata attached to the receiver's leg
 * @return a [TransactionResult] describing the outcome
 */
suspend fun OfflineCloudPlayer.transfer(
    initiator: OfflineCloudPlayer = this,
    sender: OfflineCloudPlayer = this,
    amount: Double,
    currency: Currency,
    receiver: OfflineCloudPlayer,
    ignoreSenderMinimum: Boolean = false,
    ignoreReceiverMinimum: Boolean = false,
    additionalSenderData: ObjectSet<TransactionData> = objectSetOf(),
    additionalReceiverData: ObjectSet<TransactionData> = objectSetOf()
) = transfer(
    initiator,
    sender.defaultAccount(),
    amount.toBigDecimal(),
    currency,
    receiver.defaultAccount(),
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
 * @param initiator the player initiating the transfer; defaults to `this`
 * @param sender the account sending the funds; must be non-null
 * @param amount the amount to transfer; must be non-negative
 * @param currency the monetary unit of [amount]
 * @param receiver the player receiving the funds
 * @param additionalSenderData optional metadata attached to the sender's leg
 * @param additionalReceiverData optional metadata attached to the receiver's leg
 * @return a [TransactionResult] describing the outcome
 */
suspend fun OfflineCloudPlayer.transfer(
    initiator: OfflineCloudPlayer = this,
    sender: Account,
    amount: Double,
    currency: Currency,
    receiver: Account,
    additionalSenderData: ObjectSet<TransactionData> = objectSetOf(),
    additionalReceiverData: ObjectSet<TransactionData> = objectSetOf()
) = transfer(
    initiator,
    sender,
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
 * @param account the account to check; must be non-null
 * @param currency the monetary unit of the returned balance
 * @return the current balance
 */
suspend fun OfflineCloudPlayer.balance(account: Account, currency: Currency): BigDecimal =
    InternalTransactionUserBridge.instance.balanceDecimal(account, currency)

/**
 * Convenience wrapper returning the balance of the default account as a [BigDecimal].
 *
 * @receiver the player whose balance is requested
 * @param currency the monetary unit of the returned balance
 * @return the current balance of the default account
 */
suspend fun OfflineCloudPlayer.balance(currency: Currency): BigDecimal =
    balance(defaultAccount(), currency)
// endregion