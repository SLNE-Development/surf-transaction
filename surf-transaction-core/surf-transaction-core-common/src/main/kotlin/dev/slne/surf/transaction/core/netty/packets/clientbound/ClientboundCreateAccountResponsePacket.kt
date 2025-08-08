package dev.slne.surf.transaction.core.netty.packets.clientbound

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.transaction.core.account.AccountImpl
import kotlinx.serialization.Serializable

@SurfNettyPacket("transaction:clientbound:account_response", PacketFlow.CLIENTBOUND)
@Serializable
data class ClientboundCreateAccountResponsePacket(
    val result: AccountCreationResult
) : ResponseNettyPacket() {

    @Serializable
    sealed class AccountCreationResult(val message: String) {

        @Serializable
        data class Success(val account: AccountImpl) :
            AccountCreationResult("Account created successfully.")

        @Serializable
        data class Failure(val reason: FailureReason) :
            AccountCreationResult("Failed to create account: $reason")

        @Serializable
        enum class FailureReason {
            NAME_ALREADY_EXISTS
        }
    }
}