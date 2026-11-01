package dev.slne.surf.transaction.api.currency

import java.io.Serial

/**
 * Thrown when a balance query targets a disabled currency without `ignoreDisabled = true`.
 *
 * @property currencyName the technical name of the disabled currency
 */
class CurrencyDisabledException(
    val currencyName: String
) : RuntimeException("Currency '$currencyName' is disabled") {

    companion object {
        @Serial
        private const val serialVersionUID: Long = -3164032850176390542L
    }
}
