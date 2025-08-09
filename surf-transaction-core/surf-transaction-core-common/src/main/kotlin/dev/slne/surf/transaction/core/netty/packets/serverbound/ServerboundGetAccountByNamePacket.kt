package dev.slne.surf.transaction.core.netty.packets.serverbound

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.transaction.core.netty.packets.clientbound.ClientboundAccountResponsePacket
import kotlinx.serialization.Serializable

@SurfNettyPacket("transaction:serverbound:account_get_name", PacketFlow.SERVERBOUND)
@Serializable
class ServerboundGetAccountByNamePacket(
    val accountName: String
) : RespondingNettyPacket<ClientboundAccountResponsePacket>() 