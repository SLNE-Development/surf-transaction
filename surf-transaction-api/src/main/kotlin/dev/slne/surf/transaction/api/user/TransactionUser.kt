package dev.slne.surf.transaction.api.user

import dev.slne.surf.surfapi.core.api.messages.adventure.getPointer
import dev.slne.surf.surfapi.core.api.util.objectSetOf
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.account.AccountAccess
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.transactional.Transactional
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.identity.Identity
import java.math.BigDecimal
import java.util.*

interface TransactionUser : Transactional, AccountAccess {
    override suspend fun deposit(
        account: Account,
        initiator: UUID,
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
        userUuid,
        amount,
        currency,
        ignoreMinimum,
        *additionalData
    )

    override suspend fun withdraw(
        account: Account,
        initiator: UUID,
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
        userUuid,
        amount,
        currency,
        ignoreMinimum,
        *additionalData
    )

    override suspend fun transfer(
        initiator: UUID,
        sender: Account,
        amount: BigDecimal,
        currency: Currency,
        receiver: Account,
        ignoreSenderMinimum: Boolean,
        ignoreReceiverMinimum: Boolean,
        additionalSenderData: Set<TransactionData>,
        additionalReceiverData: Set<TransactionData>
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
        additionalSenderData: Set<TransactionData> = objectSetOf(),
        additionalReceiverData: Set<TransactionData> = objectSetOf()
    ) = transfer(
        userUuid,
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
        fun byUuid(uuid: UUID): TransactionUser = TransactionUserService.instance.byUuid(uuid)
        operator fun get(uuid: UUID) = byUuid(uuid)
    }
}

fun Audience.transactionUserOrNull() = getPointer(Identity.UUID)?.let { TransactionUser.byUuid(it) }
fun Audience.transactionUser() =
    transactionUserOrNull() ?: error("Audience does not provide a uuid pointer!")