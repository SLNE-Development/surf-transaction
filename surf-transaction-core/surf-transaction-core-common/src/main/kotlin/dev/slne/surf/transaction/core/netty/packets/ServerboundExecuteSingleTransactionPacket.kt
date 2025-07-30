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

@SurfNettyPacket("transaction:serverbound:deposit", PacketFlow.SERVERBOUND)
@Serializable
data class ServerboundExecuteSingleTransactionPacket(
    val player: OfflineCloudPlayer,
    val amount: @Contextual BigDecimal,
    val currency: Currency,
    val ignoreMinimum: Boolean,
    val additionalData: Array<out TransactionData>,
    val type: Type
) : RespondingNettyPacket<TransactionResultResponsePacket>() {

    enum class Type {
        DEPOSIT,
        WITHDRAW,
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ServerboundExecuteSingleTransactionPacket) return false
        if (!super.equals(other)) return false

        if (ignoreMinimum != other.ignoreMinimum) return false
        if (player != other.player) return false
        if (amount != other.amount) return false
        if (currency != other.currency) return false
        if (!additionalData.contentEquals(other.additionalData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + ignoreMinimum.hashCode()
        result = 31 * result + player.hashCode()
        result = 31 * result + amount.hashCode()
        result = 31 * result + currency.hashCode()
        result = 31 * result + additionalData.contentHashCode()
        return result
    }
}