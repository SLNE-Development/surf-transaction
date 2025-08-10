package dev.slne.surf.transaction.core.netty.packets.serverbound

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.transaction.core.currency.CurrencyImpl
import dev.slne.surf.transaction.core.netty.packets.bidirectional.CurrencyCreateResultResponsePacket
import kotlinx.serialization.Serializable

@SurfNettyPacket("transaction:serverbound:create_currency", PacketFlow.SERVERBOUND)
@Serializable
data class ServerboundCreateCurrencyPacket(val currency: CurrencyImpl) :
    RespondingNettyPacket<CurrencyCreateResultResponsePacket>() {
}