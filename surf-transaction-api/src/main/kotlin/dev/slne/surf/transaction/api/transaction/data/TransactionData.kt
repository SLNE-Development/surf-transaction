package dev.slne.surf.transaction.api.transaction.data

import kotlinx.serialization.Serializable


@Serializable
data class TransactionData(val key: String, val value: String) {

    constructor(pair: Pair<String, String>) : this(key = pair.first, value = pair.second)
}
