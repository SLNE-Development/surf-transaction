package dev.slne.surf.transaction.server.user

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.user.InternalTransactionUserBridge
import dev.slne.surf.transaction.server.transaction.TransactionService
import it.unimi.dsi.fastutil.objects.ObjectSet
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class ServerTransactionUserBridge(private val service: TransactionService) :
    InternalTransactionUserBridge {
    override suspend fun deposit(
        player: OfflineCloudPlayer,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult = service.deposit(player, amount, currency, ignoreMinimum, *additionalData)

    override suspend fun withdraw(
        player: OfflineCloudPlayer,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult =
        service.withdraw(player, amount, currency, ignoreMinimum, *additionalData)

    override suspend fun transfer(
        sender: OfflineCloudPlayer,
        amount: BigDecimal,
        currency: Currency,
        receiver: OfflineCloudPlayer,
        ignoreSenderMinimum: Boolean,
        ignoreReceiverMinimum: Boolean,
        additionalSenderData: ObjectSet<TransactionData>,
        additionalReceiverData: ObjectSet<TransactionData>
    ): TransactionResult = service.transfer(
        sender,
        amount,
        currency,
        receiver,
        ignoreSenderMinimum,
        ignoreReceiverMinimum,
        additionalSenderData,
        additionalReceiverData
    )

    override suspend fun balanceDecimal(
        player: OfflineCloudPlayer,
        currency: Currency
    ): BigDecimal = service.balanceDecimal(player, currency)
}