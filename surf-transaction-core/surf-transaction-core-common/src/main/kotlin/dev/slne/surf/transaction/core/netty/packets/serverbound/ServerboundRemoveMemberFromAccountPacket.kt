package dev.slne.surf.transaction.core.netty.packets.serverbound

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.java.SerializableUUID
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.transaction.core.netty.packets.clientbound.ClientboundRemoveMemberFromAccountResultPacket
import kotlinx.serialization.Serializable

@Serializable
@SurfNettyPacket(id = "transaction:account:remove_member", flow = PacketFlow.SERVERBOUND)
class ServerboundRemoveMemberFromAccountPacket(
    val accountId: SerializableUUID,
    val executorId: SerializableUUID,
    val targetId: SerializableUUID
) : RespondingNettyPacket<ClientboundRemoveMemberFromAccountResultPacket>()