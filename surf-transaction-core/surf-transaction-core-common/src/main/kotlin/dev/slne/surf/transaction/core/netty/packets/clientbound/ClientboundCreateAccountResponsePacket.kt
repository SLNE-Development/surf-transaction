package dev.slne.surf.transaction.core.netty.packets.clientbound

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.transaction.api.account.AccountCreationResult
import kotlinx.serialization.Serializable

@SurfNettyPacket("transaction:clientbound:account_create_response", PacketFlow.CLIENTBOUND)
@Serializable
data class ClientboundCreateAccountResponsePacket(
    val result: AccountCreationResult
) : ResponseNettyPacket()