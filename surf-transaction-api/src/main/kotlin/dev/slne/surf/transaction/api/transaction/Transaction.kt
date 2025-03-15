package dev.slne.surf.transaction.api.transaction

import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.user.TransactionUser
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.math.BigDecimal
import java.util.*

interface Transaction {

    /**
     * A unique identifier for the transaction
     */
    val identifier: UUID

    /**
     * The sender of the transaction
     * If the sender is null, the transaction is a system transaction
     * System transactions are transactions that are not initiated by a user
     */
    val sender: TransactionUser?

    /**
     * The receiver of the transaction
     * If the receiver is null, the transaction is a system transaction
     * System transactions are transactions that are not initiated by a user
     */
    val receiver: TransactionUser?

    /**
     * The currency of the transaction
     */
    val currency: Currency

    /**
     * The amount of the transaction
     */
    val amount: BigDecimal

    /**
     * The data of the transaction
     */
    val data: ObjectSet<TransactionData>

    /**
     * If the transaction should ignore the minimum amount of the currency
     */
    val ignoreMinimumAmount: Boolean

}