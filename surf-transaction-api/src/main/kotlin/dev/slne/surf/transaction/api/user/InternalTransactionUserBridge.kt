package dev.slne.surf.transaction.api.user

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.surfapi.core.api.util.objectSetOf
import dev.slne.surf.transaction.api.InternalTransactionApiBridge
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import it.unimi.dsi.fastutil.objects.ObjectSet
import org.springframework.beans.factory.getBean
import java.math.BigDecimal

@InternalTransactionApi
interface InternalTransactionUserBridge {

    suspend fun deposit(
        player: OfflineCloudPlayer,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean = false,
        vararg additionalData: TransactionData
    ): TransactionResult

    suspend fun withdraw(
        player: OfflineCloudPlayer,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean = false,
        vararg additionalData: TransactionData
    ): TransactionResult

    suspend fun transfer(
        sender: OfflineCloudPlayer,
        amount: BigDecimal,
        currency: Currency,
        receiver: OfflineCloudPlayer,
        ignoreSenderMinimum: Boolean = false,
        ignoreReceiverMinimum: Boolean = false,
        additionalSenderData: ObjectSet<TransactionData> = objectSetOf(),
        additionalReceiverData: ObjectSet<TransactionData> = objectSetOf()
    ): TransactionResult

    suspend fun balanceDecimal(player: OfflineCloudPlayer, currency: Currency): BigDecimal


    companion object {
        val instance get() = InternalTransactionApiBridge.instance.context.getBean<InternalTransactionUserBridge>()
    }
}