package dev.slne.surf.transaction.api.transaction

import dev.slne.surf.transaction.api.util.InternalTransactionApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(InternalTransactionApi::class)
typealias SerializableTransaction = @Serializable(with = TransactionSerializer::class) Transaction

@InternalTransactionApi
object TransactionSerializer: KSerializer<Transaction> {
    override val descriptor: SerialDescriptor
        get() = InternalTransactionBridge.instance.descriptor

    override fun serialize(
        encoder: Encoder,
        value: Transaction
    ) {
        InternalTransactionBridge.instance.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): Transaction {
        return InternalTransactionBridge.instance.deserialize(decoder)
    }
}