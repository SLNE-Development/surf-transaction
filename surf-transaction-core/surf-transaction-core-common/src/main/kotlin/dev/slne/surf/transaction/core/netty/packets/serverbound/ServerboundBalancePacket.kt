package dev.slne.surf.transaction.core.netty.packets.serverbound

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.network.protocol.double.BigDecimalResponsePacket
import dev.slne.surf.transaction.api.currency.Currency
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@SurfNettyPacket("transaction:serverbound:balance", PacketFlow.SERVERBOUND)
@Serializable
data class ServerboundBalancePacket(
    val accountId: @Contextual UUID,
    val currency: Currency
) : BigDecimalResponsePacket()