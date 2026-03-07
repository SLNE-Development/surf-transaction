package dev.slne.surf.transaction.api.item

import dev.slne.surf.transaction.api.transaction.Transaction
import org.jetbrains.annotations.ApiStatus

/**
 * A [Transaction] that involves a quantity of a specific item type.
 *
 * This is used for item deposits (adding items to an account) and
 * item withdrawals (removing items from an account).
 */
@ApiStatus.NonExtendable
interface ItemTransaction : Transaction {

    /**
     * The fingerprint of the item being transacted.
     */
    val item: ItemFingerprint

    /**
     * The quantity of items in this transaction.
     *
     * Positive values indicate a deposit/credit, negative values indicate a withdrawal/debit.
     */
    val amount: Int
}