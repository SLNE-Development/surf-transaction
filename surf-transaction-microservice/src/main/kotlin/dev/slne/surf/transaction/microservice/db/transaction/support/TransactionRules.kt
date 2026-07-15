package dev.slne.surf.transaction.microservice.db.transaction.support

import dev.slne.surf.database.libs.org.jetbrains.exposed.v1.core.*
import dev.slne.surf.transaction.api.transaction.TransactionCommitResult
import dev.slne.surf.transaction.api.transaction.TransactionRollbackResult
import dev.slne.surf.transaction.api.transaction.TransactionState
import dev.slne.surf.transaction.core.common.transaction.TransactionImpl
import dev.slne.surf.transaction.microservice.db.transaction.support.TransactionRules.contributesToAvailableBalance
import dev.slne.surf.transaction.microservice.db.transaction.table.TransactionTable
import java.math.BigDecimal
import java.time.Instant

internal object TransactionRules {

    /**
     * Whether a stored transaction counts towards the available balance of its receiver account.
     *
     * Committed transactions always count. Pending ones only count while they reserve funds, i.e.
     * they have a negative amount and have not expired yet. Rolled-back and expired transactions
     * never count.
     */
    fun contributesToAvailableBalance(
        state: TransactionState,
        amount: BigDecimal,
        expiresAt: Instant?,
        now: Instant
    ): Boolean = when (state) {
        TransactionState.COMMITTED -> true
        TransactionState.PENDING -> amount < BigDecimal.ZERO && expiresAt?.isAfter(now) == true
        TransactionState.ROLLED_BACK,
        TransactionState.EXPIRED -> false
    }

    /**
     * SQL mirror of [contributesToAvailableBalance] for balance queries.
     *
     * @param now the current time expression, used to determine whether a transaction has expired
     *
     * @see contributesToAvailableBalance
     */
    fun availableBalancePredicate(now: Expression<Instant>): Op<Boolean> {
        val activePendingDebit = listOf(
            TransactionTable.state eq TransactionState.PENDING,
            TransactionTable.amount less BigDecimal.ZERO,
            TransactionTable.expiresAt.isNotNull(),
            TransactionTable.expiresAt greater now
        ).compoundAnd()

        return (TransactionTable.state eq TransactionState.COMMITTED) or activePendingDebit
    }

    /**
     * SQL mirror of [contributesToAvailableBalance] for balance queries.
     */
    fun availableBalancePredicate(now: Instant): Op<Boolean> {
        val activePendingReservation = listOf(
            TransactionTable.state eq TransactionState.PENDING,
            TransactionTable.amount less BigDecimal.ZERO,
            TransactionTable.expiresAt.isNotNull(),
            TransactionTable.expiresAt greater now
        ).compoundAnd()

        return (TransactionTable.state eq TransactionState.COMMITTED) or activePendingReservation
    }

    /**
     * Whether adding [amount] to [availableBalance] keeps the account at or above the currency's
     * [minimumAmount].
     */
    fun remainsAtOrAboveMinimum(
        availableBalance: BigDecimal,
        amount: BigDecimal,
        minimumAmount: BigDecimal
    ): Boolean = availableBalance + amount >= minimumAmount

    /**
     * Whether two transactions describe the two sides of one financial transfer: distinct
     * identifiers, same initiator and currency, a negative sender amount exactly mirrored by the
     * receiver amount, and account references that point at each other.
     */
    fun isMirroredTransferPair(
        senderTransaction: TransactionImpl,
        receiverTransaction: TransactionImpl
    ): Boolean {
        if (senderTransaction.identifier == receiverTransaction.identifier) return false
        if (senderTransaction.initiator != receiverTransaction.initiator) return false
        if (senderTransaction.currencyName != receiverTransaction.currencyName) return false

        if (senderTransaction.amount.signum() >= 0) return false
        if (receiverTransaction.amount.signum() <= 0) return false
        if (
            senderTransaction.amount.negate().compareTo(receiverTransaction.amount) != 0
        ) return false

        val senderAccountId = senderTransaction.senderAccountId ?: return false
        val senderReceiverAccountId = senderTransaction.receiverAccountId ?: return false
        val receiverSenderAccountId = receiverTransaction.senderAccountId ?: return false
        val receiverAccountId = receiverTransaction.receiverAccountId ?: return false

        if (senderAccountId != receiverAccountId) return false
        if (senderReceiverAccountId != receiverSenderAccountId) return false

        return true
    }

    /**
     * The commit result for a [transaction] that was not pending anymore when the commit was
     * attempted.
     */
    fun commitOutcome(
        transaction: TransactionImpl
    ): TransactionCommitResult = when (transaction.state) {
        TransactionState.COMMITTED -> TransactionCommitResult.AlreadyCommitted(transaction)
        TransactionState.ROLLED_BACK -> TransactionCommitResult.RolledBack(transaction)
        TransactionState.EXPIRED -> TransactionCommitResult.Expired(transaction)
        TransactionState.PENDING -> TransactionCommitResult.InvalidState(transaction)
    }

    /**
     * The rollback result for a [transaction] that was not pending anymore when the rollback was
     * attempted.
     */
    fun rollbackOutcome(
        transaction: TransactionImpl
    ): TransactionRollbackResult = when (transaction.state) {
        TransactionState.COMMITTED -> TransactionRollbackResult.Committed(transaction)
        TransactionState.ROLLED_BACK -> TransactionRollbackResult.AlreadyRolledBack(transaction)
        TransactionState.EXPIRED -> TransactionRollbackResult.Expired(transaction)
        TransactionState.PENDING -> TransactionRollbackResult.InvalidState(transaction)
    }
}
