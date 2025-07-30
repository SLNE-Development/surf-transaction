package dev.slne.surf.transaction.core.transaction

import dev.slne.surf.transaction.api.transaction.InternalTransactionBridge
import dev.slne.surf.transaction.api.transaction.Transaction
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.springframework.stereotype.Component

@Component
class TransactionBridgeImpl: InternalTransactionBridge {
    override val descriptor: SerialDescriptor
        get() = TransactionImpl.serializer().descriptor

    override fun serialize(
        encoder: Encoder,
        value: Transaction
    ) {
        require(value is TransactionImpl) { "Expected TransactionImpl, but got ${value::class.simpleName}" }
        TransactionImpl.serializer().serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): Transaction {
        return TransactionImpl.serializer().deserialize(decoder)
    }
}