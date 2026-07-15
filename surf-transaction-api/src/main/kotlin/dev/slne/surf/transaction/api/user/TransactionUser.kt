package dev.slne.surf.transaction.api.user

import dev.slne.surf.api.core.messages.adventure.getPointer
import dev.slne.surf.api.core.util.objectSetOf
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.account.AccountAccess
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.PendingTransactionResult
import dev.slne.surf.transaction.api.transaction.PendingTransactions
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.transactional.PendingExecutionDecision
import dev.slne.surf.transaction.api.transactional.PendingExecutionResult
import dev.slne.surf.transaction.api.transactional.PendingRollbackPolicy
import dev.slne.surf.transaction.api.transactional.Transactional
import dev.slne.surf.transaction.api.user.TransactionUser.Companion.byUuid
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.identity.Identity
import org.jetbrains.annotations.ApiStatus
import java.math.BigDecimal
import java.util.*
import kotlin.time.Duration

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
     * Creates a pending deposit for the user's default account.
     * Microservice and RPC failures are propagated as exceptions.
     *
     * @param amount the positive deposit amount
     * @param currency the transaction currency
     * @param timeout the persistent reservation lifetime
     * @param ignoreMinimum whether to ignore the configured minimum balance check
     * @param additionalData immutable transaction metadata
     * @return the typed reservation result
     */
    suspend fun beginDeposit(
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration = PendingTransactions.DEFAULT_TIMEOUT,
        ignoreMinimum: Boolean = false,
        vararg additionalData: TransactionData
    ): PendingTransactionResult = beginDeposit(
        getDefaultAccount(),
        userUuid,
        amount,
        currency,
        timeout,
        ignoreMinimum,
        *additionalData
    )

    /**
     * Creates a pending withdrawal for the user's default account and reserves its funds.
     * Microservice and RPC failures are propagated as exceptions.
     *
     * @param amount the positive withdrawal amount
     * @param currency the transaction currency
     * @param timeout the persistent reservation lifetime
     * @param ignoreMinimum whether to ignore the configured minimum balance check
     * @param additionalData immutable transaction metadata
     * @return the typed reservation result
     */
    suspend fun beginWithdrawal(
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration = PendingTransactions.DEFAULT_TIMEOUT,
        ignoreMinimum: Boolean = false,
        vararg additionalData: TransactionData
    ): PendingTransactionResult = beginWithdrawal(
        getDefaultAccount(),
        userUuid,
        amount,
        currency,
        timeout,
        ignoreMinimum,
        *additionalData
    )

    /**
     * Creates a pending transfer from the user's default account.
     * Microservice and RPC failures are propagated as exceptions.
     *
     * @param amount the positive transfer amount
     * @param currency the transaction currency
     * @param receiver the account credited after commit
     * @param timeout the persistent reservation lifetime
     * @param ignoreSenderMinimum whether to ignore the sender minimum balance check
     * @param ignoreReceiverMinimum whether to ignore the receiver minimum balance check
     * @param additionalSenderData sender-side immutable transaction metadata
     * @param additionalReceiverData receiver-side immutable transaction metadata
     * @return the typed reservation result
     */
    suspend fun beginTransfer(
        amount: BigDecimal,
        currency: Currency,
        receiver: Account,
        timeout: Duration = PendingTransactions.DEFAULT_TIMEOUT,
        ignoreSenderMinimum: Boolean = false,
        ignoreReceiverMinimum: Boolean = false,
        additionalSenderData: Set<TransactionData> = objectSetOf(),
        additionalReceiverData: Set<TransactionData> = objectSetOf()
    ): PendingTransactionResult = beginTransfer(
        userUuid,
        getDefaultAccount(),
        amount,
        currency,
        receiver,
        timeout,
        ignoreSenderMinimum,
        ignoreReceiverMinimum,
        additionalSenderData,
        additionalReceiverData
    )

    /**
     * Coordinates an external operation with a pending deposit on the user's default account.
     *
     * @param amount the positive deposit amount
     * @param currency the transaction currency
     * @param timeout the persistent reservation lifetime
     * @param ignoreMinimum whether to ignore the configured minimum balance check
     * @param additionalData immutable transaction metadata
     * @param rollbackOn policy identifying failures for which rollback is safe
     * @param block external operation receiving the stable transaction identifier
     * @return the typed execution result
     */
    suspend fun <T> withPendingDeposit(
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration = PendingTransactions.DEFAULT_TIMEOUT,
        ignoreMinimum: Boolean = false,
        additionalData: Set<TransactionData> = objectSetOf(),
        rollbackOn: PendingRollbackPolicy = PendingRollbackPolicy.Never,
        block: suspend (UUID) -> T
    ): PendingExecutionResult<T> = withPendingDeposit(
        account = getDefaultAccount(),
        initiator = userUuid,
        amount = amount,
        currency = currency,
        timeout = timeout,
        ignoreMinimum = ignoreMinimum,
        additionalData = additionalData,
        rollbackOn = rollbackOn,
        block = block
    )

    /**
     * Coordinates an external operation with a pending withdrawal from the user's default account.
     *
     * @param amount the positive withdrawal amount
     * @param currency the transaction currency
     * @param timeout the persistent reservation lifetime
     * @param ignoreMinimum whether to ignore the configured minimum balance check
     * @param additionalData immutable transaction metadata
     * @param rollbackOn policy identifying failures for which rollback is safe
     * @param block external operation receiving the stable transaction identifier
     * @return the typed execution result
     */
    suspend fun <T> withPendingWithdrawal(
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration = PendingTransactions.DEFAULT_TIMEOUT,
        ignoreMinimum: Boolean = false,
        additionalData: Set<TransactionData> = objectSetOf(),
        rollbackOn: PendingRollbackPolicy = PendingRollbackPolicy.Never,
        block: suspend (UUID) -> T
    ): PendingExecutionResult<T> = withPendingWithdrawal(
        account = getDefaultAccount(),
        initiator = userUuid,
        amount = amount,
        currency = currency,
        timeout = timeout,
        ignoreMinimum = ignoreMinimum,
        additionalData = additionalData,
        rollbackOn = rollbackOn,
        block = block
    )

    /**
     * Coordinates an external operation with a pending transfer from the user's default account.
     *
     * [block] receives the sender-side transaction identifier for correlation without exposing
     * manual lifecycle operations.
     *
     * @param amount the positive transfer amount
     * @param currency the transaction currency
     * @param receiver the account credited after commit
     * @param timeout the persistent reservation lifetime
     * @param ignoreSenderMinimum whether to ignore the sender minimum balance check
     * @param ignoreReceiverMinimum whether to ignore the receiver minimum balance check
     * @param additionalSenderData sender-side immutable transaction metadata
     * @param additionalReceiverData receiver-side immutable transaction metadata
     * @param rollbackOn policy identifying failures for which rollback is safe
     * @param block external operation receiving the sender transaction identifier
     * @return the typed execution result
     */
    suspend fun <T> withPendingTransfer(
        amount: BigDecimal,
        currency: Currency,
        receiver: Account,
        timeout: Duration = PendingTransactions.DEFAULT_TIMEOUT,
        ignoreSenderMinimum: Boolean = false,
        ignoreReceiverMinimum: Boolean = false,
        additionalSenderData: Set<TransactionData> = objectSetOf(),
        additionalReceiverData: Set<TransactionData> = objectSetOf(),
        rollbackOn: PendingRollbackPolicy = PendingRollbackPolicy.Never,
        block: suspend (UUID) -> T
    ): PendingExecutionResult<T> = withPendingTransfer(
        initiator = userUuid,
        sender = getDefaultAccount(),
        amount = amount,
        currency = currency,
        receiver = receiver,
        timeout = timeout,
        ignoreSenderMinimum = ignoreSenderMinimum,
        ignoreReceiverMinimum = ignoreReceiverMinimum,
        additionalSenderData = additionalSenderData,
        additionalReceiverData = additionalReceiverData,
        rollbackOn = rollbackOn,
        block = block
    )

    /**
     * Coordinates a value-based external result with a pending deposit on the default account.
     *
     * @param amount the positive deposit amount
     * @param currency the transaction currency
     * @param timeout the persistent reservation lifetime
     * @param ignoreMinimum whether to ignore the configured minimum balance check
     * @param additionalData immutable transaction metadata
     * @param rollbackOn policy identifying thrown failures for which rollback is safe
     * @param block external operation receiving the identifier and returning the lifecycle decision
     * @return the typed execution result
     */
    suspend fun <T> withPendingDepositDecision(
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration = PendingTransactions.DEFAULT_TIMEOUT,
        ignoreMinimum: Boolean = false,
        additionalData: Set<TransactionData> = objectSetOf(),
        rollbackOn: PendingRollbackPolicy = PendingRollbackPolicy.Never,
        block: suspend (UUID) -> PendingExecutionDecision<T>
    ): PendingExecutionResult<T> = withPendingDepositDecision(
        account = getDefaultAccount(),
        initiator = userUuid,
        amount = amount,
        currency = currency,
        timeout = timeout,
        ignoreMinimum = ignoreMinimum,
        additionalData = additionalData,
        rollbackOn = rollbackOn,
        block = block
    )

    /**
     * Coordinates a value-based external result with a pending withdrawal from the default account.
     *
     * For a Boolean-returning API, the block can return
     * `PendingExecutionDecision.fromBoolean(operation())`.
     *
     * @param amount the positive withdrawal amount
     * @param currency the transaction currency
     * @param timeout the persistent reservation lifetime
     * @param ignoreMinimum whether to ignore the configured minimum balance check
     * @param additionalData immutable transaction metadata
     * @param rollbackOn policy identifying thrown failures for which rollback is safe
     * @param block external operation receiving the identifier and returning the lifecycle decision
     * @return the typed execution result
     */
    suspend fun <T> withPendingWithdrawalDecision(
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration = PendingTransactions.DEFAULT_TIMEOUT,
        ignoreMinimum: Boolean = false,
        additionalData: Set<TransactionData> = objectSetOf(),
        rollbackOn: PendingRollbackPolicy = PendingRollbackPolicy.Never,
        block: suspend (UUID) -> PendingExecutionDecision<T>
    ): PendingExecutionResult<T> = withPendingWithdrawalDecision(
        account = getDefaultAccount(),
        initiator = userUuid,
        amount = amount,
        currency = currency,
        timeout = timeout,
        ignoreMinimum = ignoreMinimum,
        additionalData = additionalData,
        rollbackOn = rollbackOn,
        block = block
    )

    /**
     * Coordinates a value-based external result with a pending transfer from the default account.
     *
     * [block] receives the sender-side transaction identifier, which identifies the atomic
     * lifecycle of both transfer sides without exposing manual lifecycle operations.
     *
     * @param amount the positive transfer amount
     * @param currency the transaction currency
     * @param receiver the account credited after commit
     * @param timeout the persistent reservation lifetime
     * @param ignoreSenderMinimum whether to ignore the sender minimum balance check
     * @param ignoreReceiverMinimum whether to ignore the receiver minimum balance check
     * @param additionalSenderData sender-side immutable transaction metadata
     * @param additionalReceiverData receiver-side immutable transaction metadata
     * @param rollbackOn policy identifying thrown failures for which rollback is safe
     * @param block external operation receiving the identifier and returning the lifecycle decision
     * @return the typed execution result
     */
    suspend fun <T> withPendingTransferDecision(
        amount: BigDecimal,
        currency: Currency,
        receiver: Account,
        timeout: Duration = PendingTransactions.DEFAULT_TIMEOUT,
        ignoreSenderMinimum: Boolean = false,
        ignoreReceiverMinimum: Boolean = false,
        additionalSenderData: Set<TransactionData> = objectSetOf(),
        additionalReceiverData: Set<TransactionData> = objectSetOf(),
        rollbackOn: PendingRollbackPolicy = PendingRollbackPolicy.Never,
        block: suspend (UUID) -> PendingExecutionDecision<T>
    ): PendingExecutionResult<T> = withPendingTransferDecision(
        initiator = userUuid,
        sender = getDefaultAccount(),
        amount = amount,
        currency = currency,
        receiver = receiver,
        timeout = timeout,
        ignoreSenderMinimum = ignoreSenderMinimum,
        ignoreReceiverMinimum = ignoreReceiverMinimum,
        additionalSenderData = additionalSenderData,
        additionalReceiverData = additionalReceiverData,
        rollbackOn = rollbackOn,
        block = block
    )

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
        fun byUuid(uuid: UUID): TransactionUser = TransactionUserService.byUuid(uuid)

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
