package dev.slne.surf.transaction.api.user

import dev.slne.surf.api.core.util.requiredService
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import java.util.UUID

/**
 * Internal service for resolving [TransactionUser] instances.
 *
 * This service is used by the transaction module to obtain a user-scoped API
 * entry point for transaction and account operations.
 *
 * This API is strictly internal to the Surf Transaction module and must not be
 * used by external consumers.
 */
@InternalTransactionApi
interface TransactionUserService {

    /**
     * Resolves the [TransactionUser] for the given [uuid].
     *
     * Implementations may create the user instance lazily and/or cache it.
     *
     * @param uuid the UUID of the user
     * @return the resolved [TransactionUser]
     */
    fun byUuid(uuid: UUID): TransactionUser

    companion object {
        val instance = requiredService<TransactionUserService>()
    }
}