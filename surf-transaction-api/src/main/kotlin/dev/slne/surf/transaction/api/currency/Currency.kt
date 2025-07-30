package dev.slne.surf.transaction.api.currency

import dev.slne.surf.surfapi.core.api.messages.Colors
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.jetbrains.annotations.UnmodifiableView
import java.math.BigDecimal

/**
 * Describes a monetary unit that can be used in the transaction system.
 *
 * A **currency** is identified by its unique [name] and may provide rich‐text
 * variants for UI presentation via [displayName] and [symbolDisplay].
 * Implementations must be immutable and thread-safe.
 *
 * ### Serialization
 * This interface is annotated with `@Serializable(with = CurrencySerializer::class)`
 * to enable polymorphic (de)serialization of concrete currency implementations.
 */
@OptIn(InternalTransactionApi::class)
@Serializable(with = CurrencySerializer::class)
interface Currency {

    /** Unique identifier (≤ [ CURRENCY_NAME_MAX_LENGTH ] characters), e.g. `"castcoin"`. */
    val name: String

    /** Rich-text display name, e.g. `<red>CastCoin</red>`. */
    val displayName: Component

    /** `true` if this is the platform-wide default currency. */
    val defaultCurrency: Boolean

    /** Plain text symbol, e.g. `"$"`. */
    val symbol: String

    /** Rich-text variant of [symbol], e.g. `<red>$</red>`. */
    val symbolDisplay: Component

    /** Decimal precision and formatting rules for this currency. */
    val scale: CurrencyScale

    /** Minimum amount that must be present after validation unless bypassed. */
    val minimumAmount: BigDecimal

    /**
     * Converts [amount] into a formatted, colorized [Component].
     *
     * @param amount value to format
     * @param color  text color for the numeric part; defaults to [Colors.VARIABLE_VALUE]
     * @return formatted text like `"§7$§c1.50"`
     */
    fun format(amount: BigDecimal, color: TextColor = Colors.VARIABLE_VALUE): Component

    /**
     * Convenience overload delegating to the `BigDecimal` version.
     *
     * @param amount value to format (will be converted with `toBigDecimal()`)
     * @param color  text color for the numeric part; defaults to [Colors.VARIABLE_VALUE]
     * @return formatted text component
     */
    fun format(amount: Double, color: TextColor = Colors.VARIABLE_VALUE) =
        format(amount.toBigDecimal(), color)

    companion object {
        /** Maximum allowed length for [name]. */
        const val CURRENCY_NAME_MAX_LENGTH = 16

        /** Maximum allowed length for [symbol] and its display variant. */
        const val CURRENCY_SYMBOL_MAX_LENGTH = 16

        /** Returns the platform-wide default currency. */
        fun default(): Currency = InternalCurrencyBridge.instance.defaultCurrency

        /**
         * Immutable view of all registered currencies.
         *
         * The returned view **must not** be mutated by the caller.
         */
        fun all(): @UnmodifiableView ObjectSet<out Currency> =
            InternalCurrencyBridge.instance.currencies

        /** Retrieves a currency by its [name] or `null` if none matches. */
        fun byName(name: String): Currency? =
            InternalCurrencyBridge.instance.getCurrencyByName(name)

        /** Alias for [byName]. */
        operator fun get(name: String): Currency? = byName(name)
    }
}