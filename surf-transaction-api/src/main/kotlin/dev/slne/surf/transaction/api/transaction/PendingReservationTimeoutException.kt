package dev.slne.surf.transaction.api.transaction

import org.jetbrains.annotations.Unmodifiable
import java.io.Serial
import java.util.*

/**
 * The reservation RPC timed out before the client could confirm whether creation succeeded.
 *
 * The microservice may already have persisted the reservation. Reconcile every identifier in
 * [transactionIdentifiers] with [Transaction.byIdentifier] instead of blindly creating a
 * replacement reservation. The lookup returns `null` when the microservice did not persist that
 * identifier. For a transfer, the set contains both transaction identifiers.
 *
 * @property transactionIdentifiers stable identifiers generated for the timed-out request
 * @param cause underlying RabbitMQ request-timeout exception
 */
class PendingReservationTimeoutException(
    transactionIdentifiers: Set<UUID>,
    cause: Throwable
) : RuntimeException(
    "Pending reservation request timed out for ${transactionIdentifiers.joinToString()}",
    cause
) {
    val transactionIdentifiers: @Unmodifiable Set<UUID> = transactionIdentifiers.toSet()

    companion object {
        @Serial
        private const val serialVersionUID: Long = 5150508204778547582L
    }
}
