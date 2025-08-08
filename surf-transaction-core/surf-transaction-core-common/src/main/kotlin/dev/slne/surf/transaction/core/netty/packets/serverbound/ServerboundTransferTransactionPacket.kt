package dev.slne.surf.transaction.core.netty.packets.serverbound

import dev.slne.surf.cloud.api.common.meta.SurfNettyPacket
import dev.slne.surf.cloud.api.common.netty.network.protocol.PacketFlow
import dev.slne.surf.cloud.api.common.netty.packet.RespondingNettyPacket
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.core.netty.packets.bidirectional.TransactionResultResponsePacket
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.*

@SurfNettyPacket("transaction:serverbound:transfer", PacketFlow.SERVERBOUND)
@Serializable
data class ServerboundTransferTransactionPacket(
    val initiator: OfflineCloudPlayer?,
    val senderAccountId: @Contextual UUID,
    val amount: @Contextual BigDecimal,
    val currency: Currency,
    val receiverAccountId: @Contextual UUID,
    val ignoreSenderMinimum: Boolean,
    val ignoreReceiverMinimum: Boolean,
    val additionalSenderData: Set<TransactionData>,
    val additionalReceiverData: Set<TransactionData>
) : RespondingNettyPacket<TransactionResultResponsePacket>()
