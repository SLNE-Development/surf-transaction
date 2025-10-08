package dev.slne.surf.transaction.core.netty.packets.clientbound

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.codec.kotlinx.java.SerializableUUID
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.transaction.api.util.ComponentResult
import kotlinx.serialization.Serializable

@Serializable
@SurfNettyPacket(id = "transaction:account:add_member_result", flow = PacketFlow.CLIENTBOUND)
class ClientboundAddMemberToAccountResultPacket(
    val accountId: SerializableUUID,
    val executorId: SerializableUUID,
    val targetId: SerializableUUID,
    val result: ComponentResult
) : ResponseNettyPacket()