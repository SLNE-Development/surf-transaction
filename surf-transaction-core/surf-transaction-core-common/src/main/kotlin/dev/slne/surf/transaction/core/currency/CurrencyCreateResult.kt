package dev.slne.surf.transaction.core.currency

import kotlinx.serialization.Serializable

@Suppress("ClassName")
@Serializable
sealed class CurrencyCreateResult {
    @Serializable
    data class SUCCESS(val currency: CurrencyImpl, val default: Boolean) : CurrencyCreateResult()

    @Serializable
    data object ALREADY_EXISTS : CurrencyCreateResult()

    @Serializable
    data object DEFAULT_ALREADY_EXISTS : CurrencyCreateResult()

    @Serializable
    data object INVALID_NAME : CurrencyCreateResult()

    @Serializable
    data object INVALID_SYMBOL : CurrencyCreateResult()

    @Serializable
    data object CHANGED_DEFAULT_CURRENCY : CurrencyCreateResult()
}