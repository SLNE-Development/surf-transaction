package dev.slne.surf.transaction.api.currency

import dev.slne.surf.transaction.api.util.InternalTransactionApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@InternalTransactionApi
typealias SerializableCurrency = @Serializable(with = CurrencySerializer::class) Currency

@InternalTransactionApi
object CurrencySerializer : KSerializer<Currency> {
    override val descriptor = PrimitiveSerialDescriptor("Currency", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: Currency
    ) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): Currency {
        val name = decoder.decodeString()
        return Currency.byName(name) ?: error("Currency with name '$name' not found")
    }
}