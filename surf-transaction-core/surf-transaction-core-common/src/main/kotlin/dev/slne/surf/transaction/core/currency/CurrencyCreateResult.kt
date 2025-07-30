package dev.slne.surf.transaction.core.currency

import kotlinx.serialization.Serializable

@Suppress("ClassName")
@Serializable
sealed class CurrencyCreateResult {
    data class SUCCESS(val currency: CurrencyImpl, val default: Boolean) : CurrencyCreateResult()
    data object ALREADY_EXISTS : CurrencyCreateResult()
    data object DEFAULT_ALREADY_EXISTS : CurrencyCreateResult()
    data object INVALID_NAME : CurrencyCreateResult()
    data object INVALID_SYMBOL : CurrencyCreateResult()
    data object CHANGED_DEFAULT_CURRENCY : CurrencyCreateResult()
}