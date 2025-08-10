package dev.slne.surf.transaction.server.currency

import dev.slne.surf.cloud.api.server.plugin.CoroutineTransactional
import dev.slne.surf.surfapi.core.api.util.logger
import dev.slne.surf.surfapi.core.api.util.mutableObjectListOf
import dev.slne.surf.transaction.api.currency.Currency.Companion.CURRENCY_NAME_MAX_LENGTH
import dev.slne.surf.transaction.api.currency.Currency.Companion.CURRENCY_SYMBOL_MAX_LENGTH
import dev.slne.surf.transaction.core.currency.CurrencyCreateResult
import dev.slne.surf.transaction.core.currency.CurrencyImpl
import dev.slne.surf.transaction.server.currency.db.CurrencyEntity
import dev.slne.surf.transaction.server.currency.db.CurrencyTable
import it.unimi.dsi.fastutil.objects.ObjectList
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.springframework.stereotype.Repository

@CoroutineTransactional
@Repository
class CurrencyRepository {
    private val log = logger()

    suspend fun fetchAll(): FetchAllResult {
        val currencies = CurrencyEntity.all().toMutableList()
        val defaultCurrency = currencies.find { it.defaultCurrency }
        val finalDefaultCurrency: CurrencyImpl
        val apiCurrencies: ObjectList<CurrencyImpl>

        if (defaultCurrency != null) {
            finalDefaultCurrency = defaultCurrency.toApi()
            apiCurrencies = currencies.mapTo(mutableObjectListOf()) { it.toApi() }
        } else {
            log.atWarning()
                .log("No default currency found in the database. Creating a fallback default currency...")

            finalDefaultCurrency = CurrencyEntity.new {
                name = CurrencyImpl.DEFAULT.name
                displayName = CurrencyImpl.DEFAULT.displayName
                symbol = CurrencyImpl.DEFAULT.symbol
                symbolDisplay = CurrencyImpl.DEFAULT.symbolDisplay
                scale = CurrencyImpl.DEFAULT.scale
                this.defaultCurrency = CurrencyImpl.DEFAULT.defaultCurrency
                minimumAmount = CurrencyImpl.DEFAULT.minimumAmount
            }.toApi()
            apiCurrencies = currencies.mapTo(mutableObjectListOf()) { it.toApi() }
                .also { it.add(finalDefaultCurrency) }
        }

        return FetchAllResult(currencies = apiCurrencies, defaultCurrency = finalDefaultCurrency)
    }

    suspend fun createCurrency(
        currency: CurrencyImpl,
        overrideDefault: Boolean
    ): CurrencyCreateResult {
        val existing = CurrencyEntity
            .find { CurrencyTable.name eq currency.name }
            .forUpdate()
            .singleOrNull()

        if (existing != null) {
            if (overrideDefault) {
                CurrencyEntity.find { CurrencyTable.defaultCurrency eq true }
                    .forUpdate()
                    .singleOrNull()
                    ?.defaultCurrency = false

                existing.defaultCurrency = true
                return CurrencyCreateResult.CHANGED_DEFAULT_CURRENCY
            }

            return CurrencyCreateResult.ALREADY_EXISTS
        }

        validate(currency)?.let { return it }

        if (currency.defaultCurrency) {
            val previousDefault = CurrencyEntity.find { CurrencyTable.defaultCurrency eq true }
                .forUpdate()
                .singleOrNull()

            when {
                previousDefault == null -> Unit
                overrideDefault -> previousDefault.defaultCurrency = false
                else -> return CurrencyCreateResult.DEFAULT_ALREADY_EXISTS
            }
        }

        val newCurrency = CurrencyEntity.new {
            name = currency.name
            displayName = currency.displayName
            symbol = currency.symbol
            symbolDisplay = currency.symbolDisplay
            scale = currency.scale
            this.defaultCurrency = currency.defaultCurrency
            minimumAmount = currency.minimumAmount
        }

        return CurrencyCreateResult.SUCCESS(newCurrency.toApi(), newCurrency.defaultCurrency)
    }

    private fun validate(c: CurrencyImpl): CurrencyCreateResult? {
        fun invalidName() = c.name.isBlank() || c.name.length > CURRENCY_NAME_MAX_LENGTH
        fun invalidSymbol() = c.symbol.isBlank() || c.symbol.length > CURRENCY_SYMBOL_MAX_LENGTH

        if (invalidName()) return CurrencyCreateResult.INVALID_NAME
        if (invalidSymbol()) return CurrencyCreateResult.INVALID_SYMBOL

        val plainName = PlainTextComponentSerializer.plainText().serialize(c.displayName)
        val plainSymbol = PlainTextComponentSerializer.plainText().serialize(c.symbolDisplay)
        if (plainName.isBlank() || plainName.length > CURRENCY_NAME_MAX_LENGTH) return CurrencyCreateResult.INVALID_NAME
        if (plainSymbol.isBlank() || plainSymbol.length > CURRENCY_SYMBOL_MAX_LENGTH) return CurrencyCreateResult.INVALID_SYMBOL
        return null
    }

    suspend fun fetchCurrencyByName(name: String): CurrencyEntity? = CurrencyEntity
        .find { CurrencyTable.name like name }
        .singleOrNull()


    data class FetchAllResult(
        val currencies: ObjectList<CurrencyImpl>,
        val defaultCurrency: CurrencyImpl,
    )
}