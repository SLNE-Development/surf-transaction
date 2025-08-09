package dev.slne.surf.transaction.core.netty.packets.serverbound

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.transaction.core.netty.packets.clientbound.ClientboundAllAccountsResponsePacket
import kotlinx.serialization.Serializable

@SurfNettyPacket("transaction:serverbound:account_get_all", PacketFlow.SERVERBOUND)
@Serializable
class ServerboundGetAllAccountsPacket(
    val owner: OfflineCloudPlayer
) : RespondingNettyPacket<ClientboundAllAccountsResponsePacket>()