package dev.slne.surf.transaction.core.netty.packets.serverbound

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.transaction.core.netty.packets.clientbound.ClientboundDeleteAccountResponsePacket
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@SurfNettyPacket("transaction:serverbound:account_delete", PacketFlow.SERVERBOUND)
@Serializable
class ServerboundDeleteAccountPacket(
    val accountId: @Contextual UUID
) : RespondingNettyPacket<ClientboundDeleteAccountResponsePacket>()