package dev.slne.surf.transaction.api.transaction.data

import kotlinx.serialization.Serializable

/**
 * Represents a key-value metadata entry attached to a transaction.
 *
 * [TransactionData] is used to store additional contextual information
 * related to a transaction, such as its source, purpose, or custom identifiers.
 *
 * Instances must be created using the factory methods provided in the
 * companion object.
 *
 * This class is serializable and intended for safe transport and persistence.
 *
 * @property key the metadata key
 * @property value the metadata value
 */
@ConsistentCopyVisibility
@Serializable
data class TransactionData private constructor(val key: String, val value: String) {

    companion object {
        /**
         * Creates a new [TransactionData] instance from the given key and value.
         *
         * @param key the metadata key
         * @param value the metadata value
         */
        fun of(key: String, value: String) = TransactionData(key, value)

        /**
         * Creates a new [TransactionData] instance from a key-value [Pair].
         *
         * @param pair the key-value pair to convert
         */
        fun byPair(pair: Pair<String, String>) = TransactionData(pair.first, pair.second)
    }
}
