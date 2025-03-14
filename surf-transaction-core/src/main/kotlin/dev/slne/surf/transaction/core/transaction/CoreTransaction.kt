package dev.slne.surf.transaction.core.transaction

import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.Transaction
import dev.slne.surf.transaction.api.user.TransactionUser
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import java.math.BigDecimal
import java.util.*

/**
 * A core implementation of [Transaction]
 */
class CoreTransaction(
    override val identifier: UUID,
    override val sender: TransactionUser?,
    override val receiver: TransactionUser?,
    override val currency: Currency,
    override val amount: BigDecimal,
) : Transaction {

    val data = TransactionData()

    @Suppress("UNCHECKED_CAST")
    @OptIn(InternalSerializationApi::class)
    override fun <T : Any> addData(data: T) {
        val serializer: KSerializer<T> = data::class.serializer() as KSerializer<T>

        this.data.data.add(Json.encodeToJsonElement(serializer, data))
    }
}

@Serializable
data class TransactionData(val data: MutableSet<JsonElement> = mutableSetOf())