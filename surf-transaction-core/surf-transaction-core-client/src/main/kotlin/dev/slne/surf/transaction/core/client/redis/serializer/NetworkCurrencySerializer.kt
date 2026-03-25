package dev.slne.surf.transaction.core.client.redis.serializer

import dev.slne.surf.transaction.api.currency.Currency
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

typealias NetworkCurrency = @Serializable(with = NetworkCurrencySerializer::class) Currency

object NetworkCurrencySerializer : KSerializer<Currency> {
    override val descriptor =
        PrimitiveSerialDescriptor("transaction.Currency", PrimitiveKind.STRING)

    override fun serialize(
        encoder: Encoder,
        value: Currency
    ) {
        encoder.encodeString(value.name)
    }

    override fun deserialize(decoder: Decoder): Currency {
        val currency = Currency.byName(decoder.decodeString())
        requireNotNull(currency) { "Currency not found" }
        return currency
    }
}