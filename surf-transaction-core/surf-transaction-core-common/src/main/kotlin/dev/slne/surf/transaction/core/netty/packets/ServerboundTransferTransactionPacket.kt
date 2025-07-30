package dev.slne.surf.transaction.core.netty.packets

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal

@SurfNettyPacket("transaction:serverbound:transfer", PacketFlow.SERVERBOUND)
@Serializable
data class ServerboundTransferTransactionPacket(
    val sender: OfflineCloudPlayer,
    val amount: @Contextual BigDecimal,
    val currency: Currency,
    val receiver: OfflineCloudPlayer,
    val ignoreSenderMinimum: Boolean,
    val ignoreReceiverMinimum: Boolean,
    val additionalSenderData: Set<TransactionData>,
    val additionalReceiverData: Set<TransactionData>
) : RespondingNettyPacket<TransactionResultResponsePacket>()
