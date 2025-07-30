package dev.slne.surf.transaction.core.netty.packets

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.network.protocol.double.BigDecimalResponsePacket
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.transaction.api.currency.Currency
import kotlinx.serialization.Serializable

@SurfNettyPacket("transaction:serverbound:balance", PacketFlow.SERVERBOUND)
@Serializable
data class ServerboundBalancePacket(
    val player: OfflineCloudPlayer,
    val currency: Currency
) : BigDecimalResponsePacket()