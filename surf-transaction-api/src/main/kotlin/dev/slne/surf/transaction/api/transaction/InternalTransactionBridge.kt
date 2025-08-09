package dev.slne.surf.transaction.api.transaction

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.surfapi.core.api.util.objectSetOf
import dev.slne.surf.transaction.api.InternalTransactionApiBridge
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import it.unimi.dsi.fastutil.objects.ObjectSet
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.springframework.beans.factory.getBean
import java.math.BigDecimal

@InternalTransactionApi
interface InternalTransactionBridge {

    val descriptor: SerialDescriptor
    fun serialize(encoder: Encoder, value: Transaction)
    fun deserialize(decoder: Decoder): Transaction

    suspend fun deposit(
        account: Account,
        initiator: OfflineCloudPlayer,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean = false,
        vararg additionalData: TransactionData
    ): TransactionResult

    suspend fun withdraw(
        account: Account,
        initiator: OfflineCloudPlayer,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean = false,
        vararg additionalData: TransactionData
    ): TransactionResult

    suspend fun transfer(
        initiator: OfflineCloudPlayer,
        sender: Account,
        amount: BigDecimal,
        currency: Currency,
        receiver: Account,
        ignoreSenderMinimum: Boolean = false,
        ignoreReceiverMinimum: Boolean = false,
        additionalSenderData: ObjectSet<TransactionData> = objectSetOf(),
        additionalReceiverData: ObjectSet<TransactionData> = objectSetOf()
    ): TransactionResult

    suspend fun balance(account: Account, currency: Currency): BigDecimal

    companion object {
        val instance get() = InternalTransactionApiBridge.instance.context.getBean<InternalTransactionBridge>()
    }
}