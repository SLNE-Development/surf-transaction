package dev.slne.surf.transaction.core.client.user

import dev.slne.surf.cloud.api.client.netty.packet.awaitOrThrow
import dev.slne.surf.cloud.api.client.netty.packet.fireAndAwaitOrThrow
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.user.InternalTransactionUserBridge
import dev.slne.surf.transaction.core.netty.packets.ServerboundBalancePacket
import dev.slne.surf.transaction.core.netty.packets.ServerboundExecuteSingleTransactionPacket
import dev.slne.surf.transaction.core.netty.packets.ServerboundTransferTransactionPacket
import it.unimi.dsi.fastutil.objects.ObjectSet
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class ClientTransactionUserBridge : InternalTransactionUserBridge {
    override suspend fun deposit(
        player: OfflineCloudPlayer,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult = ServerboundExecuteSingleTransactionPacket(
        player,
        amount,
        currency,
        ignoreMinimum,
        additionalData,
        ServerboundExecuteSingleTransactionPacket.Type.DEPOSIT
    ).fireAndAwaitOrThrow().result


    override suspend fun withdraw(
        player: OfflineCloudPlayer,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult = ServerboundExecuteSingleTransactionPacket(
        player,
        amount,
        currency,
        ignoreMinimum,
        additionalData,
        ServerboundExecuteSingleTransactionPacket.Type.WITHDRAW
    ).fireAndAwaitOrThrow().result

    override suspend fun transfer(
        sender: OfflineCloudPlayer,
        amount: BigDecimal,
        currency: Currency,
        receiver: OfflineCloudPlayer,
        ignoreSenderMinimum: Boolean,
        ignoreReceiverMinimum: Boolean,
        additionalSenderData: ObjectSet<TransactionData>,
        additionalReceiverData: ObjectSet<TransactionData>
    ): TransactionResult = ServerboundTransferTransactionPacket(
        sender,
        amount,
        currency,
        receiver,
        ignoreSenderMinimum,
        ignoreReceiverMinimum,
        additionalSenderData,
        additionalReceiverData
    ).fireAndAwaitOrThrow().result

    override suspend fun balanceDecimal(
        player: OfflineCloudPlayer,
        currency: Currency
    ): BigDecimal = ServerboundBalancePacket(player, currency).awaitOrThrow()
}