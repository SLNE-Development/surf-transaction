package dev.slne.surf.transaction.api.transactional

import dev.slne.surf.api.core.util.objectSetOf
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.PendingTransactionResult
import dev.slne.surf.transaction.api.transaction.PendingTransactions
import dev.slne.surf.transaction.api.transaction.PendingReservationTimeoutException
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import org.jetbrains.annotations.ApiStatus
import java.math.BigDecimal
import java.util.UUID
import kotlin.time.Duration

/**
 * Defines transactional operations that can be performed on accounts.
 *
 * A [Transactional] implementation provides suspendable methods for modifying account balances,
 * including deposits, withdrawals, transfers, and durable pending reservations. Immediate
 * operations return [TransactionResult]; pending workflows use their dedicated typed results.
 *
 * Pending creation and explicit lifecycle calls use RabbitMQ RPC and propagate microservice or
 * transport exceptions. A creation timeout is exposed as [PendingReservationTimeoutException] so
 * its generated identifiers remain available for reconciliation. Once an automatic `withPending*`
 * block has run, commit and rollback exceptions are retained in [PendingExecutionResult] so the
 * external and financial outcomes can be reconciled together.
 */
@OptIn(InternalTransactionApi::class)
@ApiStatus.NonExtendable
interface Transactional {

    /**
     * Creates a durable pending deposit that does not become spendable until commit.
     * Microservice and RPC failures are propagated as exceptions.
     *
     * @param account the target account
     * @param initiator the entity initiating the reservation
     * @param amount the positive amount to deposit
     * @param currency the transaction currency
     * @param timeout the persistent reservation lifetime
     * @param ignoreMinimum whether to ignore the configured minimum balance check
     * @param additionalData immutable audit metadata
     * @return the typed reservation result
     */
    suspend fun beginDeposit(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration = PendingTransactions.DEFAULT_TIMEOUT,
        ignoreMinimum: Boolean = false,
        vararg additionalData: TransactionData
    ): PendingTransactionResult

    /**
     * Atomically creates a durable pending withdrawal and reserves spendable funds immediately.
     * Microservice and RPC failures are propagated as exceptions.
     *
     * @param account the account whose funds are reserved
     * @param initiator the entity initiating the reservation
     * @param amount the positive amount to reserve
     * @param currency the transaction currency
     * @param timeout the persistent reservation lifetime
     * @param ignoreMinimum whether to ignore the configured minimum balance check
     * @param additionalData immutable audit metadata
     * @return the typed reservation result
     */
    suspend fun beginWithdrawal(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration = PendingTransactions.DEFAULT_TIMEOUT,
        ignoreMinimum: Boolean = false,
        vararg additionalData: TransactionData
    ): PendingTransactionResult

    /**
     * Atomically creates both sides of a durable pending transfer.
     *
     * The sender amount is reserved immediately while the receiver amount remains unavailable until
     * commit. A rollback releases the sender reservation without crediting the receiver.
     * Microservice and RPC failures are propagated as exceptions.
     *
     * @param initiator the entity initiating the reservation
     * @param sender the account whose funds are reserved
     * @param amount the positive transfer amount
     * @param currency the transaction currency
     * @param receiver the account credited only after commit
     * @param timeout the persistent reservation lifetime
     * @param ignoreSenderMinimum whether to ignore the sender minimum balance check
     * @param ignoreReceiverMinimum whether to ignore the receiver minimum balance check
     * @param additionalSenderData sender-side audit metadata
     * @param additionalReceiverData receiver-side audit metadata
     * @return the typed reservation result
     */
    suspend fun beginTransfer(
        initiator: UUID,
        sender: Account,
        amount: BigDecimal,
        currency: Currency,
        receiver: Account,
        timeout: Duration = PendingTransactions.DEFAULT_TIMEOUT,
        ignoreSenderMinimum: Boolean = false,
        ignoreReceiverMinimum: Boolean = false,
        additionalSenderData: Set<TransactionData> = objectSetOf(),
        additionalReceiverData: Set<TransactionData> = objectSetOf()
    ): PendingTransactionResult

    /**
     * Reserves a deposit, executes [block], and commits on normal completion.
     *
     * [block] is never invoked when reservation fails. A non-fatal exception matched by
     * [rollbackOn] triggers a rollback attempt. Unmatched exceptions, coroutine cancellation, and
     * fatal JVM errors preserve the pending reservation and are rethrown. A commit failure after
     * [block] succeeds is returned and never followed by an automatic rollback.
     * Use [withPendingDepositDecision] when normal return values can also represent failure. The
     * block receives the stable transaction identifier for correlation or idempotency, but the
     * lifecycle transition remains owned by this method.
     *
     * @param account the account that receives the deposit after commit
     * @param initiator the entity initiating the transaction
     * @param amount the positive deposit amount
     * @param currency the transaction currency
     * @param timeout the persistent reservation lifetime
     * @param ignoreMinimum whether to ignore the configured minimum balance check
     * @param additionalData immutable transaction metadata
     * @param rollbackOn policy identifying failures for which rollback is safe
     * @param block external suspending operation receiving the stable transaction identifier
     * @return the typed execution result
     */
    suspend fun <T> withPendingDeposit(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration = PendingTransactions.DEFAULT_TIMEOUT,
        ignoreMinimum: Boolean = false,
        additionalData: Set<TransactionData> = objectSetOf(),
        rollbackOn: PendingRollbackPolicy = PendingRollbackPolicy.Never,
        block: suspend (UUID) -> T
    ): PendingExecutionResult<T>

    /**
     * Reserves a withdrawal, executes [block], and commits on normal completion.
     *
     * [block] is never invoked when reservation fails. A non-fatal exception matched by
     * [rollbackOn] triggers a rollback attempt. Unmatched exceptions, coroutine cancellation, and
     * fatal JVM errors preserve the pending reservation and are rethrown. A commit failure after
     * [block] succeeds is returned and never followed by an automatic rollback.
     * Use [withPendingWithdrawalDecision] when normal return values can also represent failure. The
     * block receives the stable transaction identifier for correlation or idempotency, but the
     * lifecycle transition remains owned by this method.
     *
     * @param account the account whose spendable funds are reserved
     * @param initiator the entity initiating the transaction
     * @param amount the positive withdrawal amount
     * @param currency the transaction currency
     * @param timeout the persistent reservation lifetime
     * @param ignoreMinimum whether to ignore the configured minimum balance check
     * @param additionalData immutable transaction metadata
     * @param rollbackOn policy identifying failures for which rollback is safe
     * @param block external suspending operation receiving the stable transaction identifier
     * @return the typed execution result
     */
    suspend fun <T> withPendingWithdrawal(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration = PendingTransactions.DEFAULT_TIMEOUT,
        ignoreMinimum: Boolean = false,
        additionalData: Set<TransactionData> = objectSetOf(),
        rollbackOn: PendingRollbackPolicy = PendingRollbackPolicy.Never,
        block: suspend (UUID) -> T
    ): PendingExecutionResult<T>

    /**
     * Reserves a transfer, executes [block], and commits both sides on normal completion.
     *
     * [block] receives the sender-side transaction identifier, which identifies the atomic
     * lifecycle of both transfer sides without exposing manual commit or rollback operations.
     * [block] is never invoked when reservation fails. A non-fatal exception matched by [rollbackOn]
     * triggers a rollback attempt. Unmatched exceptions, coroutine cancellation, and fatal JVM
     * errors preserve the pending reservation and are rethrown. A commit failure after [block]
     * succeeds is returned and never followed by an automatic rollback. Use
     * [withPendingTransferDecision] when normal return values can also represent failure.
     *
     * @param initiator the entity initiating the transaction
     * @param sender the account whose spendable funds are reserved
     * @param amount the positive transfer amount
     * @param currency the transaction currency
     * @param receiver the account credited after commit
     * @param timeout the persistent reservation lifetime
     * @param ignoreSenderMinimum whether to ignore the sender minimum balance check
     * @param ignoreReceiverMinimum whether to ignore the receiver minimum balance check
     * @param additionalSenderData sender-side immutable transaction metadata
     * @param additionalReceiverData receiver-side immutable transaction metadata
     * @param rollbackOn policy identifying failures for which rollback is safe
     * @param block external suspending operation receiving the sender transaction identifier
     * @return the typed execution result
     */
    suspend fun <T> withPendingTransfer(
        initiator: UUID,
        sender: Account,
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
    ): PendingExecutionResult<T>

    /**
     * Reserves a deposit and lets [block] explicitly select commit or rollback from its normal
     * return value.
     *
     * This variant is intended for external APIs that report success through values such as a
     * Boolean, status enum, or sealed result instead of throwing. The selected lifecycle transition
     * is executed exactly once. Exceptions continue to follow [rollbackOn]; unmatched exceptions,
     * cancellation, and fatal JVM errors preserve the pending reservation and are rethrown.
     *
     * @param account the account that receives the deposit after commit
     * @param initiator the entity initiating the transaction
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
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration = PendingTransactions.DEFAULT_TIMEOUT,
        ignoreMinimum: Boolean = false,
        additionalData: Set<TransactionData> = objectSetOf(),
        rollbackOn: PendingRollbackPolicy = PendingRollbackPolicy.Never,
        block: suspend (UUID) -> PendingExecutionDecision<T>
    ): PendingExecutionResult<T>

    /**
     * Reserves a withdrawal and lets [block] explicitly select commit or rollback from its normal
     * return value.
     *
     * This variant is intended for external APIs that report success through values such as a
     * Boolean, status enum, or sealed result instead of throwing. The selected lifecycle transition
     * is executed exactly once. Exceptions continue to follow [rollbackOn]; unmatched exceptions,
     * cancellation, and fatal JVM errors preserve the pending reservation and are rethrown.
     *
     * @param account the account whose spendable funds are reserved
     * @param initiator the entity initiating the transaction
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
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        timeout: Duration = PendingTransactions.DEFAULT_TIMEOUT,
        ignoreMinimum: Boolean = false,
        additionalData: Set<TransactionData> = objectSetOf(),
        rollbackOn: PendingRollbackPolicy = PendingRollbackPolicy.Never,
        block: suspend (UUID) -> PendingExecutionDecision<T>
    ): PendingExecutionResult<T>

    /**
     * Reserves a transfer and lets [block] explicitly select commit or rollback from its normal
     * return value.
     *
     * The identifier passed to [block] belongs to the sender-side transaction and identifies the
     * atomic lifecycle of both transfer sides. This variant is intended for external APIs that
     * report success through values such as a Boolean, status enum, or sealed result instead of
     * throwing.
     * The selected transition is executed exactly once. Exceptions continue to follow [rollbackOn];
     * unmatched exceptions, cancellation, and fatal JVM errors preserve the reservation and are
     * rethrown.
     *
     * @param initiator the entity initiating the transaction
     * @param sender the account whose spendable funds are reserved
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
        initiator: UUID,
        sender: Account,
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
    ): PendingExecutionResult<T>

    /**
     * Deposits an amount into the given [account].
     *
     * @param account the target account receiving the funds
     * @param initiator the unique identifier of the entity initiating the transaction
     * @param amount the amount to deposit
     * @param currency the currency of the amount
     * @param ignoreMinimum whether the currency minimum amount should be ignored
     * @param additionalData optional additional transaction metadata
     *
     * @return the result of the transaction
     */
    suspend fun deposit(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean = false,
        vararg additionalData: TransactionData
    ): TransactionResult

    /**
     * Withdraws an amount from the given [account].
     *
     * @param account the source account from which funds are withdrawn
     * @param initiator the unique identifier of the entity initiating the transaction
     * @param amount the amount to withdraw
     * @param currency the currency of the amount
     * @param ignoreMinimum whether the currency minimum amount should be ignored
     * @param additionalData optional additional transaction metadata
     *
     * @return the result of the transaction
     */
    suspend fun withdraw(
        account: Account,
        initiator: UUID,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean = false,
        vararg additionalData: TransactionData
    ): TransactionResult

    /**
     * Transfers an amount from one account to another.
     *
     * This operation withdraws the amount from the [sender] account and deposits
     * it into the [receiver] account as a single transactional operation.
     *
     * @param initiator the unique identifier of the entity initiating the transaction
     * @param sender the account sending the funds
     * @param amount the amount to transfer
     * @param currency the currency of the amount
     * @param receiver the account receiving the funds
     * @param ignoreSenderMinimum whether the sender's currency minimum should be ignored
     * @param ignoreReceiverMinimum whether the receiver's currency minimum should be ignored
     * @param additionalSenderData additional transaction metadata for the sender side
     * @param additionalReceiverData additional transaction metadata for the receiver side
     *
     * @return the result of the transaction
     */
    suspend fun transfer(
        initiator: UUID,
        sender: Account,
        amount: BigDecimal,
        currency: Currency,
        receiver: Account,
        ignoreSenderMinimum: Boolean = false,
        ignoreReceiverMinimum: Boolean = false,
        additionalSenderData: Set<TransactionData> = objectSetOf(),
        additionalReceiverData: Set<TransactionData> = objectSetOf()
    ): TransactionResult

    /**
     * Returns the current balance of the given [account] for the specified [currency].
     *
     * @param account the account whose balance should be queried
     * @param currency the currency of the balance
     *
     * @return the current balance
     */
    suspend fun balance(account: Account, currency: Currency): BigDecimal
}
