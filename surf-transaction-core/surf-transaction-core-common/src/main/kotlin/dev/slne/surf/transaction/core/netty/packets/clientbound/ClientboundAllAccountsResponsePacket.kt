package dev.slne.surf.transaction.core.netty.packets.clientbound

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.transaction.core.account.AccountImpl
import kotlinx.serialization.Serializable

@SurfNettyPacket("transaction:clientbound:account_all_response", PacketFlow.CLIENTBOUND)
@Serializable
data class ClientboundAllAccountsResponsePacket(
    val accounts: Set<AccountImpl>
) : ResponseNettyPacket()