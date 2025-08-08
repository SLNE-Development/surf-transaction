package dev.slne.surf.transaction.core.netty.packets.serverbound

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.transaction.core.netty.packets.clientbound.ClientboundAccountResponsePacket
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@SurfNettyPacket("transaction:serverbound:account_get", PacketFlow.SERVERBOUND)
@Serializable
class ServerboundGetAccountPacket(
    val accountId: @Contextual UUID
) : RespondingNettyPacket<ClientboundAccountResponsePacket>() 