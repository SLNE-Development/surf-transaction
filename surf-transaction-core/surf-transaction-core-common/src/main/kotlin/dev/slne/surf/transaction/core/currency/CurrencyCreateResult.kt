package dev.slne.surf.transaction.core.currency

import kotlinx.serialization.Serializable

@Serializable
enum class CurrencyCreateResult {
    SUCCESS,
    ALREADY_EXISTS,
    DEFAULT_ALREADY_EXISTS,
    INVALID_NAME,
    INVALID_SYMBOL
}
