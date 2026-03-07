package dev.slne.surf.transaction.api.transactional

import dev.slne.surf.surfapi.core.api.util.objectSetOf
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.item.ItemFingerprint
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.transaction.result.ItemTransactionResult
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import org.jetbrains.annotations.ApiStatus
import java.util.UUID

/**
 * Defines item-based transactional operations that can be performed on accounts.
 *
 * Item transactions track quantities of specific item types identified
 * by [ItemFingerprint]. The account itself is not bound to a specific item –
 * any account can hold any item type.
 */
@OptIn(InternalTransactionApi::class)
@ApiStatus.NonExtendable
interface ItemTransactional {

    /**
     * Deposits [amount] of the given [item] into the given [account].
     *
     * @param account the target account receiving the items
     * @param initiator the UUID of the entity initiating the deposit
     * @param item the fingerprint of the item type to deposit
     * @param amount the number of items to deposit (must be positive)
     * @param additionalData optional additional transaction metadata
     * @return the result of the item transaction
     */
    suspend fun depositItems(
        account: Account,
        initiator: UUID,
        item: ItemFingerprint,
        amount: Int,
        vararg additionalData: TransactionData
    ): ItemTransactionResult

    /**
     * Withdraws [amount] of the given [item] from the given [account].
     *
     * @param account the target account from which items are withdrawn
     * @param initiator the UUID of the entity initiating the withdrawal
     * @param item the fingerprint of the item type to withdraw
     * @param amount the number of items to withdraw (must be positive)
     * @param additionalData optional additional transaction metadata
     * @return the result of the item transaction
     */
    suspend fun withdrawItems(
        account: Account,
        initiator: UUID,
        item: ItemFingerprint,
        amount: Int,
        vararg additionalData: TransactionData
    ): ItemTransactionResult

    /**
     * Transfers [amount] of the given [item] from [sender] to [receiver].
     *
     * @param initiator the UUID of the entity initiating the transfer
     * @param sender the account sending the items
     * @param receiver the account receiving the items
     * @param item the fingerprint of the item type to transfer
     * @param amount the number of items to transfer (must be positive)
     * @param additionalSenderData optional metadata for the sender transaction
     * @param additionalReceiverData optional metadata for the receiver transaction
     * @return the result of the item transaction
     */
    suspend fun transferItems(
        initiator: UUID,
        sender: Account,
        receiver: Account,
        item: ItemFingerprint,
        amount: Int,
        additionalSenderData: Set<TransactionData> = objectSetOf(),
        additionalReceiverData: Set<TransactionData> = objectSetOf()
    ): ItemTransactionResult

    /**
     * Returns the current item balance (quantity) of the given [item] in the given [account].
     */
    suspend fun itemBalance(account: Account, item: ItemFingerprint): Int
}