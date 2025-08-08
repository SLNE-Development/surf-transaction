package dev.slne.surf.transaction.core.client.user

import dev.slne.surf.cloud.api.client.netty.packet.awaitOrThrow
import dev.slne.surf.cloud.api.client.netty.packet.fireAndAwaitOrThrow
import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.user.InternalTransactionUserBridge
import dev.slne.surf.transaction.core.netty.packets.serverbound.ServerboundBalancePacket
import dev.slne.surf.transaction.core.netty.packets.serverbound.ServerboundExecuteSingleTransactionPacket
import dev.slne.surf.transaction.core.netty.packets.serverbound.ServerboundGetDefaultAccountPacket
import dev.slne.surf.transaction.core.netty.packets.serverbound.ServerboundTransferTransactionPacket
import it.unimi.dsi.fastutil.objects.ObjectSet
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class ClientTransactionUserBridge : InternalTransactionUserBridge {

    override suspend fun deposit(
        account: Account,
        initiator: OfflineCloudPlayer?,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult = ServerboundExecuteSingleTransactionPacket(
        account.accountId,
        initiator,
        amount,
        currency,
        ignoreMinimum,
        additionalData,
        ServerboundExecuteSingleTransactionPacket.Type.DEPOSIT
    ).fireAndAwaitOrThrow().result

    override suspend fun withdraw(
        account: Account,
        initiator: OfflineCloudPlayer?,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult = ServerboundExecuteSingleTransactionPacket(
        account.accountId,
        initiator,
        amount,
        currency,
        ignoreMinimum,
        additionalData,
        ServerboundExecuteSingleTransactionPacket.Type.WITHDRAW
    ).fireAndAwaitOrThrow().result

    override suspend fun transfer(
        initiator: OfflineCloudPlayer?,
        sender: Account,
        amount: BigDecimal,
        currency: Currency,
        receiver: Account,
        ignoreSenderMinimum: Boolean,
        ignoreReceiverMinimum: Boolean,
        additionalSenderData: ObjectSet<TransactionData>,
        additionalReceiverData: ObjectSet<TransactionData>
    ): TransactionResult = ServerboundTransferTransactionPacket(
        initiator,
        sender.accountId,
        amount,
        currency,
        receiver.accountId,
        ignoreSenderMinimum,
        ignoreReceiverMinimum,
        additionalSenderData,
        additionalReceiverData
    ).fireAndAwaitOrThrow().result

    override suspend fun balanceDecimal(
        account: Account,
        currency: Currency
    ): BigDecimal = ServerboundBalancePacket(account.accountId, currency).awaitOrThrow()

    override suspend fun getDefaultAccountOrNull(player: OfflineCloudPlayer): Account? =
        ServerboundGetDefaultAccountPacket(player).fireAndAwaitOrThrow().account
}