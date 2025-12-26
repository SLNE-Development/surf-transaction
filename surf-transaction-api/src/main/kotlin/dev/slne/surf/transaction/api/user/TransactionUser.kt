package dev.slne.surf.transaction.api.user

import dev.slne.surf.surfapi.core.api.messages.adventure.getPointer
import dev.slne.surf.surfapi.core.api.util.objectSetOf
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.account.AccountAccess
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.transactional.Transactional
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.identity.Identity
import org.jetbrains.annotations.ApiStatus
import java.math.BigDecimal
import java.util.*

/**
 * Represents a transactional user within the transaction system.
 *
 * A [TransactionUser] combines [Transactional] and [AccountAccess] functionality
 * and provides convenience methods that automatically operate on the user's
 * default account.
 *
 * This interface is intended to simplify common user-centric transaction flows,
 * such as depositing to or withdrawing from the default account.
 */
@ApiStatus.NonExtendable
interface TransactionUser : Transactional, AccountAccess {

    /**
     * Deposits an amount into the user's default account.
     *
     * The user is automatically used as the transaction initiator.
     *
     * @param amount the amount to deposit
     * @param currency the currency of the amount
     * @param ignoreMinimum whether the currency minimum should be ignored
     * @param additionalData optional additional transaction metadata
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

    /**
     * Withdraws an amount from the user's default account.
     *
     * The user is automatically used as the transaction initiator.
     *
     * @param amount the amount to withdraw
     * @param currency the currency of the amount
     * @param ignoreMinimum whether the currency minimum should be ignored
     * @param additionalData optional additional transaction metadata
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

    /**
     * Transfers an amount from the user's default account to the given [receiver].
     *
     * The user is automatically used as the transaction initiator and sender.
     *
     * @param amount the amount to transfer
     * @param currency the currency of the amount
     * @param receiver the receiving account
     * @param ignoreSenderMinimum whether the sender minimum should be ignored
     * @param ignoreReceiverMinimum whether the receiver minimum should be ignored
     * @param additionalSenderData additional metadata for the sender transaction
     * @param additionalReceiverData additional metadata for the receiver transaction
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

    /**
     * Returns the balance of the user's default account in the specified [currency].
     *
     * @param currency the currency of the balance
     * @return the current balance
     */
    suspend fun balance(
        currency: Currency
    ): BigDecimal = balance(getDefaultAccount(), currency)

    @OptIn(InternalTransactionApi::class)
    companion object {
        /**
         * Returns a [TransactionUser] for the given [uuid].
         */
        fun byUuid(uuid: UUID): TransactionUser = TransactionUserService.instance.byUuid(uuid)

        /**
         * Shortcut operator for [byUuid].
         */
        operator fun get(uuid: UUID) = byUuid(uuid)
    }
}

/**
 * Attempts to resolve a [TransactionUser] from this [Audience].
 *
 * The audience must provide an [Identity.UUID] pointer.
 *
 * @return the resolved [TransactionUser], or `null` if no UUID is present
 */
fun Audience.transactionUserOrNull() = getPointer(Identity.UUID)?.let { TransactionUser.byUuid(it) }

/**
 * Resolves a [TransactionUser] from this [Audience].
 *
 * @throws IllegalStateException if the audience does not provide a UUID pointer
 */
fun Audience.transactionUser() =
    transactionUserOrNull() ?: error("Audience does not provide a uuid pointer!")