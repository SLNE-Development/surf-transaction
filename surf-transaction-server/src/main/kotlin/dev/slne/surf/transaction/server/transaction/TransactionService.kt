package dev.slne.surf.transaction.server.transaction

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.surfapi.core.api.util.toMutableObjectSet
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.core.transaction.TransactionImpl
import it.unimi.dsi.fastutil.objects.ObjectSet
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.*

@Service
class TransactionService(private val transactionRepository: TransactionRepository) {

    suspend fun deposit(
        player: OfflineCloudPlayer,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult {
        val transaction = TransactionImpl(
            identifier = UUID.randomUUID(),
            senderUuid = null,
            receiverUuid = player.uuid,
            amount = amount,
            currencyName = currency.name,
            ignoreMinimumAmount = ignoreMinimum,
            data = additionalData.toMutableObjectSet()
        )

        return transactionRepository.persistTransaction(transaction)
    }

    suspend fun withdraw(
        player: OfflineCloudPlayer,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult {
        val usableAmount = amount.abs().negate()

        val transaction = TransactionImpl(
            identifier = UUID.randomUUID(),
            senderUuid = null,
            receiverUuid = player.uuid,
            amount = usableAmount,
            currencyName = currency.name,
            ignoreMinimumAmount = ignoreMinimum,
            data = additionalData.toMutableObjectSet()
        )

        return transactionRepository.persistTransaction(transaction)
    }

    suspend fun transfer(
        sender: OfflineCloudPlayer,
        amount: BigDecimal,
        currency: Currency,
        receiver: OfflineCloudPlayer,
        ignoreSenderMinimum: Boolean,
        ignoreReceiverMinimum: Boolean,
        additionalSenderData: ObjectSet<TransactionData>,
        additionalReceiverData: ObjectSet<TransactionData>
    ): TransactionResult {
        val senderAmount = amount.abs().negate()
        val receiverAmount = amount.abs()

        val senderTransaction = TransactionImpl(
            identifier = UUID.randomUUID(),
            senderUuid = receiver.uuid,
            receiverUuid = sender.uuid,
            amount = senderAmount,
            currencyName = currency.name,
            ignoreMinimumAmount = ignoreSenderMinimum,
            data = additionalSenderData.toMutableObjectSet()
        )

        val receiverTransaction = TransactionImpl(
            identifier = UUID.randomUUID(),
            senderUuid = sender.uuid,
            receiverUuid = receiver.uuid,
            amount = receiverAmount,
            currencyName = currency.name,
            ignoreMinimumAmount = ignoreReceiverMinimum,
            data = additionalReceiverData.toMutableObjectSet()
        )

        return transactionRepository.transfer(senderTransaction, receiverTransaction)
    }


    suspend fun balanceDecimal(
        player: OfflineCloudPlayer,
        currency: Currency
    ): BigDecimal = transactionRepository.balanceDecimal(player, currency)
}