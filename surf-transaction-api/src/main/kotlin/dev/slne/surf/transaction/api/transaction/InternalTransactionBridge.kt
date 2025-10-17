package dev.slne.surf.transaction.api.transaction

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.surfapi.core.api.util.objectSetOf
import dev.slne.surf.transaction.api.InternalTransactionApiBridge
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.springframework.beans.factory.getBean
import java.math.BigDecimal

@InternalTransactionApi
interface InternalTransactionBridge {

    val descriptor: SerialDescriptor
    fun serialize(encoder: Encoder, value: Transaction)
    fun deserialize(decoder: Decoder): Transaction

    /**
     * Processes a deposit transaction for the specified account.
     *
     * @param account The account to which the deposit will be made.
     * @param initiator The player initiating the deposit.
     * @param amount The amount to be deposited.
     * @param currency The currency in which the deposit is made.
     * @param ignoreMinimum Whether to ignore the minimum balance requirement for the deposit.
     * @param additionalData Additional data related to the transaction.
     * @return A result indicating the success or failure of the deposit transaction.
     */
    suspend fun deposit(
        account: Account,
        initiator: OfflineCloudPlayer,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean = false,
        vararg additionalData: TransactionData
    ): TransactionResult

    /**
     * Processes a withdrawal transaction for the specified account.
     *
     * @param account The account from which the withdrawal will be made.
     * @param initiator The player initiating the withdrawal.
     * @param amount The amount to be withdrawn.
     * @param currency The currency in which the withdrawal is made.
     * @param ignoreMinimum Whether to ignore the minimum balance requirement for the withdrawal.
     * @param additionalData Additional data related to the transaction.
     * @return A result indicating the success or failure of the withdrawal transaction.
     */
    suspend fun withdraw(
        account: Account,
        initiator: OfflineCloudPlayer,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean = false,
        vararg additionalData: TransactionData
    ): TransactionResult

    /**
     * Transfers an amount from one account to another.
     *
     * @param initiator The player initiating the transfer.
     * @param sender The account from which the amount will be transferred.
     * @param amount The amount to be transferred.
     * @param currency The currency in which the transfer is made.
     * @param receiver The account to which the amount will be transferred.
     * @param ignoreSenderMinimum Whether to ignore the minimum balance requirement for the sender.
     * @param ignoreReceiverMinimum Whether to ignore the minimum balance requirement for the receiver.
     * @param additionalSenderData Additional data related to the sender's transaction.
     * @param additionalReceiverData Additional data related to the receiver's transaction.
     * @return A result indicating the success or failure of the transfer transaction.
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
    ): TransactionResult

    /**
     * Retrieves the balance of the specified account in the given currency.
     *
     * @param account The account for which to retrieve the balance.
     * @param currency The currency in which to retrieve the balance.
     * @return The balance amount as a [BigDecimal].
     */
    suspend fun balance(account: Account, currency: Currency): BigDecimal

    companion object {
        val instance get() = InternalTransactionApiBridge.instance.context.getBean<InternalTransactionBridge>()
    }
}