package dev.slne.surf.transaction.core.netty.packets.serverbound

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.core.netty.packets.bidirectional.CurrencyCreateResultResponsePacket
import kotlinx.serialization.Serializable

@SurfNettyPacket("transaction:serverbound:make_default_currency", PacketFlow.SERVERBOUND)
@Serializable
data class ServerboundMakeDefaultCurrencyPacket(val currency: Currency) :
    RespondingNettyPacket<CurrencyCreateResultResponsePacket>()