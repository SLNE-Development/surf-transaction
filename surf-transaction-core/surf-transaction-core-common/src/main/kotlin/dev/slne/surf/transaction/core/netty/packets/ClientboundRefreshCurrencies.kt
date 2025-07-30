package dev.slne.surf.transaction.core.netty.packets

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.NettyPacket
import dev.slne.surf.transaction.core.currency.CurrencyImpl
import kotlinx.serialization.Serializable

@SurfNettyPacket("transaction:clientbound:refresh_currencies", PacketFlow.CLIENTBOUND)
@Serializable
data class ClientboundRefreshCurrencies(val currencies: Set<CurrencyImpl>) : NettyPacket()