package dev.slne.surf.transaction.core.currency

import kotlinx.serialization.Serializable

@Serializable
enum class CurrencyDefaultResult {
    SUCCESS,
    NOT_FOUND
}
