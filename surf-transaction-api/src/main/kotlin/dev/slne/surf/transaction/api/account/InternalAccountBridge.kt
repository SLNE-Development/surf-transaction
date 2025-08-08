package dev.slne.surf.transaction.api.account

import dev.slne.surf.transaction.api.InternalTransactionApiBridge
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import org.springframework.beans.factory.getBean
import java.util.*

@InternalTransactionApi
interface InternalAccountBridge {
    /**
     * Retrieves an account by its unique identifier.
     *
     * @param accountId The unique identifier of the account to retrieve.
     * @return The account associated with the given identifier, or null if no such account exists.
     */
    suspend fun getAccountByAccountId(accountId: UUID): Account?

    companion object {
        val instance get() = InternalTransactionApiBridge.instance.context.getBean<InternalAccountBridge>()
    }
}