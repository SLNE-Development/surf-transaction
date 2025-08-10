package dev.slne.surf.transaction.api.account

import dev.slne.surf.transaction.api.util.InternalTransactionApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@OptIn(InternalTransactionApi::class)
object AccountSerializer : KSerializer<Account> {
    override val descriptor get() = InternalAccountBridge.instance.descriptor

    override fun serialize(
        encoder: Encoder,
        value: Account
    ) {
        InternalAccountBridge.instance.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): Account {
        return InternalAccountBridge.instance.deserialize(decoder)
    }
}