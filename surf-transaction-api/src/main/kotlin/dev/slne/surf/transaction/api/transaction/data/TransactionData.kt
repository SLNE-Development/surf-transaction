package dev.slne.surf.transaction.api.transaction.data

import kotlinx.serialization.Serializable

/**
 * Immutable key–value metadata attached to a transaction.
 *
 * * Keys are **case-sensitive** and should be unique within a single transaction.
 * * Values are stored as raw strings; callers are responsible for any type conversion.
 *
 * @property key   identifier of the entry (e.g. `"reason"`, `"source"`)
 * @property value associated value (e.g. `"daily_bonus"`)
 *
 * @constructor creates a data pair from explicit [key] and [value].
 */
@Serializable
data class TransactionData(val key: String, val value: String) {

    /**
     * Convenience constructor allowing creation from a Kotlin [Pair].
     *
     * ```kotlin
     * val data = TransactionData("category" to "shop_purchase")
     * ```
     */
    constructor(pair: Pair<String, String>) : this(key = pair.first, value = pair.second)
}
