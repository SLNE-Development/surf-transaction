package dev.slne.surf.transaction.api.account

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import java.util.*

@OptIn(InternalTransactionApi::class)
interface Account {

    /**
     * Unique identifier for the account.
     */
    val accountId: UUID

    /**
     * The name of the account.
     */
    val name: String

    /**
     * The owner of the account.
     */
    val owner: OfflineCloudPlayer

    companion object {
        /**
         * Retrieves an account by its unique identifier.
         *
         * @param accountId The unique identifier of the account to retrieve.
         * @return The [Account] associated with the given [accountId], or null if no such account exists.
         */
        suspend operator fun get(accountId: UUID?): Account? =
            accountId?.let { InternalAccountBridge.instance.getAccountByAccountId(it) }
    }

}