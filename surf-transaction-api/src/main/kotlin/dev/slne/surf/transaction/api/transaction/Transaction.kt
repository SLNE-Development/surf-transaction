package dev.slne.surf.transaction.api.transaction

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.serialization.Serializable
import org.jetbrains.annotations.Unmodifiable
import java.math.BigDecimal
import java.util.*

/**
 * Immutable description of a single monetary operation.
 *
 * A transaction moves an [amount] of a specified [currency] from an optional [sender]
 * to an optional [receiver]. When either participant is `null`, the transfer is treated
 * as a **system transaction**—originating from or destined to the platform itself
 * rather than a player.
 *
 * All properties are read-only; implementations are expected to be thread-safe.
 */
@OptIn(InternalTransactionApi::class)
@Serializable(with = TransactionSerializer::class)
interface Transaction {

    /** Globally unique identifier of this transaction instance. */
    val identifier: UUID

    /**
     * Player initiating the transfer, or `null` for system credits.
     *
     * When `null`, the funds originate from the platform (e.g. daily rewards).
     */
    val sender: OfflineCloudPlayer?

    /**
     * Player receiving the funds, or `null` for system debits.
     *
     * When `null`, the funds are removed from circulation by the platform
     * (e.g., taxes, sinks).
     */
    val receiver: OfflineCloudPlayer?

    /** Currency in which the [amount] is denominated. */
    val currency: Currency

    /** Absolute value transferred, expressed with high-precision decimal arithmetic. */
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