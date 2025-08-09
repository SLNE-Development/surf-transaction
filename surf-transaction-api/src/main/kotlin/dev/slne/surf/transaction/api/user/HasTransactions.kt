package dev.slne.surf.transaction.api.user

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.surfapi.core.api.util.objectSetOf
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.InternalTransactionBridge
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.math.BigDecimal

@OptIn(InternalTransactionApi::class)
interface HasTransactions {

    /**
     * Deposits [amount] into this player's account in the given [currency].
     *
     * @param account the account to deposit into; must be non-null
     * @param initiator the player initiating the deposit; defaults to `this`
     * @param amount the amount to deposit; must be non-negative
     * @param currency the monetary unit of [amount]
     * @param ignoreMinimum `true` to bypass minimum-balance validation
     * @param additionalData optional metadata attached to the transaction
     * @return a [TransactionResult] describing the outcome
     */
    suspend fun deposit(
        account: Account,
        initiator: OfflineCloudPlayer,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean = false,
        vararg additionalData: TransactionData
    ): TransactionResult = InternalTransactionBridge.instance.deposit(
        account,
        initiator,
        amount,
        currency,
        ignoreMinimum,
        *additionalData
    )

    /**
     * Withdraws [amount] from this player's account in the given [currency].
     *
     * @param account the account to withdraw from; must be non-null
     * @param initiator the player initiating the withdrawal; defaults to `this`
     * @param amount the amount to withdraw; must be non-negative
     * @param currency the monetary unit of [amount]
     * @param ignoreMinimum `true` to bypass minimum-balance validation
     * @param additionalData optional metadata attached to the transaction
     * @return a [TransactionResult] describing the outcome
     */
    suspend fun withdraw(
        account: Account,
        initiator: OfflineCloudPlayer,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean = false,
        vararg additionalData: TransactionData
    ): TransactionResult = InternalTransactionBridge.instance.withdraw(
        account,
        initiator,
        amount,
        currency,
        ignoreMinimum,
        *additionalData
    )

    /**
     * Transfers [amount] from this player's account to [receiver].
     *
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
    suspend fun transfer(
        initiator: OfflineCloudPlayer,
        sender: Account,
        amount: BigDecimal,
        currency: Currency,
        receiver: Account,
        ignoreSenderMinimum: Boolean = false,
        ignoreReceiverMinimum: Boolean = false,
        additionalSenderData: ObjectSet<TransactionData> = objectSetOf(),
        additionalReceiverData: ObjectSet<TransactionData> = objectSetOf()
    ): TransactionResult = InternalTransactionBridge.instance.transfer(
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
     * Retrieves this player's balance in [currency] as a [BigDecimal].
     *
     * @param account the account to check; must be non-null
     * @param currency the monetary unit of the returned balance
     * @return the current balance
     */
    suspend fun balance(account: Account, currency: Currency): BigDecimal =
        InternalTransactionBridge.instance.balance(account, currency)

}