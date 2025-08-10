package dev.slne.surf.transaction.api.user

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.util.objectSetOf
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.account.HasAccounts
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.math.BigDecimal

interface TransactionUser : HasTransactions, HasAccounts {

    override val cloudPlayer: OfflineCloudPlayer

    override suspend fun deposit(
        account: Account,
        initiator: OfflineCloudPlayer,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ) = getDefaultAccount().deposit(
        account,
        initiator,
        amount,
        currency,
        ignoreMinimum,
        *additionalData
    )

    /**
     * Deposits an amount into the default account of this user in the specified currency.
     *
     * @param amount The amount to deposit.
     * @param currency The currency in which the deposit is made.
     * @param ignoreMinimum Whether to bypass the minimum balance validation.
     * @param additionalData Additional data related to the transaction.
     * @return A [TransactionResult] describing the outcome of the deposit.
     */
    suspend fun deposit(
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean = false,
        vararg additionalData: TransactionData
    ) = deposit(
        getDefaultAccount(),
        cloudPlayer,
        amount,
        currency,
        ignoreMinimum,
        *additionalData
    )

    override suspend fun withdraw(
        account: Account,
        initiator: OfflineCloudPlayer,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ) = getDefaultAccount().withdraw(
        account,
        initiator,
        amount,
        currency,
        ignoreMinimum,
        *additionalData
    )

    /**
     * Withdraws an amount from the default account of this user in the specified currency.
     *
     * @param amount The amount to withdraw.
     * @param currency The currency in which the withdrawal is made.
     * @param ignoreMinimum Whether to bypass the minimum balance validation.
     * @param additionalData Additional data related to the transaction.
     * @return A [TransactionResult] describing the outcome of the withdrawal.
     */
    suspend fun withdraw(
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean = false,
        vararg additionalData: TransactionData
    ) = withdraw(
        getDefaultAccount(),
        cloudPlayer,
        amount,
        currency,
        ignoreMinimum,
        *additionalData
    )

    override suspend fun transfer(
        initiator: OfflineCloudPlayer,
        sender: Account,
        amount: BigDecimal,
        currency: Currency,
        receiver: Account,
        ignoreSenderMinimum: Boolean,
        ignoreReceiverMinimum: Boolean,
        additionalSenderData: ObjectSet<TransactionData>,
        additionalReceiverData: ObjectSet<TransactionData>
    ) = getDefaultAccount().transfer(
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
     * Transfers an amount from the default account of this user to the specified receiver account.
     *
     * @param amount The amount to transfer.
     * @param currency The currency in which the transfer is made.
     * @param receiver The account receiving the funds.
     * @param ignoreSenderMinimum Whether to bypass the sender's minimum balance validation.
     * @param ignoreReceiverMinimum Whether to bypass the receiver's minimum balance validation.
     * @param additionalSenderData Additional data related to the sender's transaction.
     * @param additionalReceiverData Additional data related to the receiver's transaction.
     * @return A [TransactionResult] describing the outcome of the transfer.
     */
    suspend fun transfer(
        amount: BigDecimal,
        currency: Currency,
        receiver: Account,
        ignoreSenderMinimum: Boolean = false,
        ignoreReceiverMinimum: Boolean = false,
        additionalSenderData: ObjectSet<TransactionData> = objectSetOf(),
        additionalReceiverData: ObjectSet<TransactionData> = objectSetOf()
    ) =
        transfer(
            cloudPlayer,
            getDefaultAccount(),
            amount,
            currency,
            receiver,
            ignoreSenderMinimum,
            ignoreReceiverMinimum,
            additionalSenderData,
            additionalReceiverData
        )

    override suspend fun balance(
        account: Account,
        currency: Currency
    ) = getDefaultAccount().balance(account, currency)

    /**
     * Retrieves the balance of the default account for this user in the specified currency.
     *
     * @param currency The currency in which the balance is to be retrieved.
     * @return The balance of the default account in the specified currency as a [BigDecimal].
     */
    suspend fun balance(
        currency: Currency
    ): BigDecimal = balance(getDefaultAccount(), currency)

    @OptIn(InternalTransactionApi::class)
    companion object {
        /**
         * Creates a [TransactionUser] instance for the given [OfflineCloudPlayer].
         *
         * @param cloudPlayer The offline cloud player for whom the transaction user is created.
         * @return A [TransactionUser] instance representing the specified cloud player.
         */
        operator fun invoke(cloudPlayer: OfflineCloudPlayer): TransactionUser =
            TransactionUserImpl(cloudPlayer)

        /**
         * Internal implementation of [TransactionUser].
         * This class is not intended for public use and should only be accessed through the [TransactionUser] interface.
         *
         * @property cloudPlayer The offline cloud player for this transaction user.
         */
        internal class TransactionUserImpl(
            override val cloudPlayer: OfflineCloudPlayer
        ) : TransactionUser
    }

}

/**
 * Extension function to create a [TransactionUser] from an [OfflineCloudPlayer].
 *
 * @receiver The offline cloud player for whom the transaction user is created.
 * @return A [TransactionUser] instance representing the specified cloud player.
 */
fun OfflineCloudPlayer.transactionUser() = TransactionUser(this)