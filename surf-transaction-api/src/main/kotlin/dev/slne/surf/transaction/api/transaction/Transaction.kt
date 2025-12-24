package dev.slne.surf.transaction.api.transaction

import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import org.jetbrains.annotations.Unmodifiable
import java.math.BigDecimal
import java.util.*

/**
 * Immutable description of a single monetary operation.
 *
 * A transaction moves an [amount] of a specified [currency] from an optional [senderAccount]
 * to an optional [receiverAccount]. When either participant is `null`, the transfer is treated
 * as a **system transaction**—originating from or destined to the platform itself
 * rather than an [Account].
 *
 * All properties are read-only; implementations are expected to be thread-safe.
 */
@OptIn(InternalTransactionApi::class)
interface Transaction {

    /** Globally unique identifier of this transaction instance. */
    val identifier: UUID

    /**
     * The [OfflineCloudPlayer] who initiated this transaction.
     */
    val initiator: UUID?

    /**
     * The uuid of the [Account] initiating the transfer, or `null` for system credits.
     *
     * When `null`, the funds originate from the platform (e.g. daily rewards).
     */
    val senderAccountId: UUID?

    /**
     * The uuid of the [Account] receiving the funds, or `null` for system debits.
     *
     * When `null`, the funds are removed from circulation by the platform
     * (e.g., taxes, sinks).
     */
    val receiverAccountId: UUID?

    /**
     * Asynchronously retrieves the [Account] initiating the transfer, or `null` for system credits.
     *
     * When `null`, the funds originate from the platform (e.g. daily rewards).
     */
    suspend fun senderAccount(): Account?

    /**
     * Asynchronously retrieves the [Account] receiving the funds, or `null` for system debits.
     *
     * When `null`, the funds are removed from circulation by the platform
     * (e.g., taxes, sinks).
     */
    suspend fun receiverAccount(): Account?

    /**
     * Currency in which the [amount] is denominated.
     * */
    val currency: Currency

    /**
     * Absolute value transferred, expressed with high-precision decimal arithmetic.
     */
    val amount: BigDecimal

    /**
     * Arbitrary, immutable set of metadata entries associated with this transaction.
     */
    val data: @Unmodifiable Set<TransactionData>

    /**
     * `true` if minimum-balance validation should be bypassed for this transaction.
     *
     * Typically used for administrative or system-level operations.
     */
    val ignoreMinimumAmount: Boolean

}