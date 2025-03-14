package dev.slne.surf.transaction.core.transaction

import dev.slne.surf.surfapi.core.api.util.requiredService
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.Transaction
import dev.slne.surf.transaction.api.transaction.TransactionResultType
import dev.slne.surf.transaction.api.user.TransactionUser
import java.math.BigDecimal
import java.util.*

/**
 * A service to handle transactions
 */
interface TransactionService {

    /**
     * Get the balance of a user
     *
     * @param user The user to get the balance of
     * @param currency The currency to get the balance in
     *
     * @return The balance of the user
     */
    suspend fun balanceDecimal(user: TransactionUser, currency: Currency): BigDecimal

    /**
     * Execute a transaction
     *
     * @param transaction The transaction to execute
     *
     * @return The result of the transaction
     */
    suspend fun persistTransaction(transaction: Transaction): TransactionResultType

    /**
     * Transfer money from one user to another transactionally
     *
     * The transactions will be persisted if the transfer was successful
     * If there was an error during the transfer, both transactions will be rolled back
     *
     * @param senderTransaction The transaction of the sender
     * @param receiverTransaction The transaction of the receiver
     *
     * @return The result of the transfer
     */
    suspend fun transfer(
        senderTransaction: Transaction,
        receiverTransaction: Transaction
    ): TransactionResultType

    /**
     * Generate a new transaction id
     *
     * @param excludingIds The ids to exclude from the generation, if one of the ids is generated, a new id will be generated
     *
     * @return The generated transaction id
     */
    suspend fun generateTransactionId(vararg excludingIds: UUID): UUID

    companion object {
        /**
         * The instance of the TransactionService
         */
        val INSTANCE = requiredService<TransactionService>()
    }

}

/**
 * The instance of the TransactionService
 */
val transactionService get() = TransactionService.INSTANCE