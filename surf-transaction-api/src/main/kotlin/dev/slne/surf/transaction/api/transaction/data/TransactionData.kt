package dev.slne.surf.transaction.api.transaction.data

import kotlinx.serialization.Serializable

/**
 * Represents a key-value pair of transaction data
 *
 * @param key The key of the data
 * @param value The value of the data
 */
@Serializable
data class TransactionData(val key: String, val value: String)
