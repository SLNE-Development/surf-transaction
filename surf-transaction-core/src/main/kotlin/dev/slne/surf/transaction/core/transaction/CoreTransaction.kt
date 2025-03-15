package dev.slne.surf.transaction.core.transaction

import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.Transaction
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.user.TransactionUser
import java.math.BigDecimal
import java.util.*

/**
 * A core implementation of [Transaction]
 */
class CoreTransaction(
    override val identifier: UUID,
    override val sender: TransactionUser?,
    override val receiver: TransactionUser?,
    override val currency: Currency,
    override val amount: BigDecimal,
) : Transaction {
    override val data = mutableObjectSetOf<TransactionData>()
}