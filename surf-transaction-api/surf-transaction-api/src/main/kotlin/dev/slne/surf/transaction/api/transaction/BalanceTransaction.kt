package dev.slne.surf.transaction.api.transaction

import dev.slne.surf.transaction.api.currency.Currency
import org.jetbrains.annotations.ApiStatus
import java.math.BigDecimal

/**
 * A [Transaction] that involves a monetary amount in a specific [Currency].
 *
 * This is used for deposits, withdrawals, and transfers of currency balances.
 */
@ApiStatus.NonExtendable
interface BalanceTransaction : Transaction {

    /**
     * The currency used in this transaction.
     */
    val currency: Currency

    /**
     * The monetary amount of this transaction.
     *
     * Positive values indicate a deposit/credit, negative values indicate a withdrawal/debit.
     */
    val amount: BigDecimal

    /**
     * Whether the minimum currency amount check was ignored for this transaction.
     */
    val ignoreMinimumAmount: Boolean
}