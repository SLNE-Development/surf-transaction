package dev.slne.surf.transaction.core.netty.packets.serverbound

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.transaction.core.netty.packets.clientbound.ClientboundCreateAccountResponsePacket
import kotlinx.serialization.Serializable

@SurfNettyPacket("transaction:serverbound:account_create", PacketFlow.SERVERBOUND)
@Serializable
class ServerboundCreateAccountPacket(
    val owner: OfflineCloudPlayer,
    val name: String,
) : RespondingNettyPacket<ClientboundCreateAccountResponsePacket>()