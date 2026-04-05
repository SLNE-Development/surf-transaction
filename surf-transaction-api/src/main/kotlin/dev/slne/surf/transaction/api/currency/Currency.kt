package dev.slne.surf.transaction.api.currency

import dev.slne.surf.api.core.messages.Colors
import dev.slne.surf.transaction.api.currency.Currency.Companion.CURRENCY_NAME_MAX_LENGTH
import dev.slne.surf.transaction.api.currency.Currency.Companion.CURRENCY_SYMBOL_MAX_LENGTH
import dev.slne.surf.transaction.api.currency.Currency.Companion.byName
import dev.slne.surf.transaction.api.currency.Currency.Companion.default
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.TextColor
import org.jetbrains.annotations.ApiStatus
import java.math.BigDecimal

/**
 * Represents a currency that can be used within the transaction system.
 *
 * A [Currency] defines how monetary values are identified, displayed, formatted,
 * and validated. It also implements [ComponentLike], allowing it to be directly
 * rendered as a text component (defaults to [displayName]).
 *
 * Implementations are managed by the [CurrencyService] and should not be created
 * manually.
 */
@ApiStatus.NonExtendable
@OptIn(InternalTransactionApi::class)
interface Currency : ComponentLike {
    /**
     * The unique technical name of this currency.
     *
     * This value is used for identification and lookup and must not exceed
     * [CURRENCY_NAME_MAX_LENGTH] characters.
     */
    val name: String

    /**
     * The human-readable display name of this currency.
     *
     * This component is used when the currency itself is rendered as text.
     */
    val displayName: Component

    /**
     * Whether this currency is the default currency of the system.
     *
     * The default currency is returned by [default].
     */
    val defaultCurrency: Boolean

    /**
     * The textual symbol of this currency (e.g. "$", "€", "¥").
     *
     * This value must not exceed [CURRENCY_SYMBOL_MAX_LENGTH] characters.
     */
    val symbol: String

    /**
     * The formatted component representation of the currency symbol.
     *
     * This component is typically used when formatting amounts.
     */
    val symbolDisplay: Component

    /**
     * The scale configuration of this currency.
     *
     * The scale defines how many fractional digits are supported and how
     * values are rounded or normalized.
     */
    val scale: CurrencyScale

    /**
     * The minimum allowed amount for this currency.
     *
     * Any transaction amount below this value is considered invalid.
     */
    val minimumAmount: BigDecimal

    /**
     * Formats the given [amount] as a currency component.
     *
     * The amount is scaled according to [scale] and rendered together with
     * the currency symbol.
     *
     * @param amount the monetary value to format
     * @param color the text color applied to the numeric value
     * @return a formatted currency component
     */
    fun format(amount: BigDecimal, color: TextColor = Colors.VARIABLE_VALUE): Component

    /**
     * Formats the given [amount] as a currency component.
     *
     * This is a convenience overload that converts the value to [BigDecimal]
     * before formatting.
     *
     * @param amount the monetary value to format
     * @param color the text color applied to the numeric value
     * @return a formatted currency component
     */
    fun format(amount: Double, color: TextColor = Colors.VARIABLE_VALUE) =
        format(amount.toBigDecimal(), color)

    override fun asComponent(): Component = displayName

    companion object {
        /**
         * The maximum allowed length of a currency name.
         */
        const val CURRENCY_NAME_MAX_LENGTH = 16

        /**
         * The maximum allowed length of a currency symbol.
         */
        const val CURRENCY_SYMBOL_MAX_LENGTH = 16

        /**
         * Returns the system's default currency.
         */
        fun default(): Currency = CurrencyService.defaultCurrency

        /**
         * Returns all registered currencies.
         */
        fun all(): Set<Currency> = CurrencyService.currencies

        /**
         * Returns a currency by its [name], or `null` if none exists.
         *
         * @param name the technical name of the currency
         */
        fun byName(name: String): Currency? = CurrencyService.getCurrencyByName(name)

        /**
         * Shortcut operator for [byName].
         *
         * @param name the technical name of the currency
         */
        operator fun get(name: String): Currency? = byName(name)
    }
}