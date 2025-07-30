package dev.slne.surf.transaction.core.netty.packets

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.ResponseNettyPacket
import dev.slne.surf.transaction.api.transaction.TransactionResult
import kotlinx.serialization.Serializable

@SurfNettyPacket("transaction:response:transaction_result", PacketFlow.BIDIRECTIONAL)
@Serializable
data class TransactionResultResponsePacket(val result: TransactionResult) : ResponseNettyPacket()