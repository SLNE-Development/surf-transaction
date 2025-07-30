package dev.slne.surf.transaction.core.netty.packets

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.transaction.api.currency.Currency
import kotlinx.serialization.Serializable

@SurfNettyPacket("transaction:serverbound:make_default_currency", PacketFlow.SERVERBOUND)
@Serializable
data class ServerboundMakeDefaultCurrencyPacket(val currency: Currency) :
    RespondingNettyPacket<CurrencyCreateResultResponsePacket>()