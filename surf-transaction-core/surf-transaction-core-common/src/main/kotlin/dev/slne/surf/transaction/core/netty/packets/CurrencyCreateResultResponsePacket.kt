package dev.slne.surf.transaction.core.netty.packets

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.transaction.core.currency.CurrencyCreateResult
import kotlinx.serialization.Serializable

@SurfNettyPacket("transaction:bidirectional:currency_create_result", PacketFlow.BIDIRECTIONAL)
@Serializable
data class CurrencyCreateResultResponsePacket(val result: CurrencyCreateResult): ResponseNettyPacket()