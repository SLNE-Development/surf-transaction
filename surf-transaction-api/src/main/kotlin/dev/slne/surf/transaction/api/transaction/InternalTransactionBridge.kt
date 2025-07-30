package dev.slne.surf.transaction.api.transaction

import dev.slne.surf.transaction.api.InternalTransactionApiBridge
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.springframework.beans.factory.getBean

@InternalTransactionApi
interface InternalTransactionBridge {
    val descriptor: SerialDescriptor
    fun serialize(encoder: Encoder, value: Transaction)
    fun deserialize(decoder: Decoder): Transaction

    companion object {
        val instance get() = InternalTransactionApiBridge.instance.context.getBean<InternalTransactionBridge>()
    }
}