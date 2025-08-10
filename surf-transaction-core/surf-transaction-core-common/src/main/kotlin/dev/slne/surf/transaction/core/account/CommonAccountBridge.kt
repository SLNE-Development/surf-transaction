package dev.slne.surf.transaction.core.account

import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.account.InternalAccountBridge
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

abstract class CommonAccountBridge : InternalAccountBridge {
    override val descriptor: SerialDescriptor
        get() = AccountImpl.serializer().descriptor

    override fun serialize(encoder: Encoder, value: Account) {
        require(value is AccountImpl) { "Value must be of type AccountImpl" }
        AccountImpl.serializer().serialize(encoder, value)
    }
    
    override fun deserialize(decoder: Decoder): Account {
        return AccountImpl.serializer().deserialize(decoder)
    }
}