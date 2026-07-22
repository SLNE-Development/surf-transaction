package dev.slne.surf.transaction.api.transaction

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Shared configuration for pending financial transactions.
 */
object PendingTransactions {
    /**
     * The default lifetime of a pending reservation.
     *
     * Callers should choose a shorter or longer timeout when the external operation has a known
     * service-level deadline.
     */
    // slightly longer than the default rabbitmq request timeout
    val DEFAULT_TIMEOUT: Duration = 62.seconds
}
