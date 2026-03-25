package dev.slne.surf.transaction.core.common.currency

enum class CurrencyCreateResult {
    SUCCESS,
    ALREADY_EXISTS,
    DEFAULT_ALREADY_EXISTS,
    INVALID_NAME,
    INVALID_SYMBOL
}