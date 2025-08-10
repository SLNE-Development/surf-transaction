package dev.slne.surf.transaction.server.transaction

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.core.transaction.CommonTransactionBridgeImpl
import it.unimi.dsi.fastutil.objects.ObjectSet
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class ServerTransactionBridge(
    private val transactionService: TransactionService
) : CommonTransactionBridgeImpl() {
    override suspend fun deposit(
        account: Account,
        initiator: OfflineCloudPlayer,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ) = transactionService.deposit(
        initiator,
        account.accountId,
        amount,
        currency,
        ignoreMinimum,
        *additionalData
    )

    override suspend fun withdraw(
        account: Account,
        initiator: OfflineCloudPlayer,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ) = transactionService.withdraw(
        initiator,
        account.accountId,
        amount,
        currency,
        ignoreMinimum,
        *additionalData
    )

    override suspend fun transfer(
        initiator: OfflineCloudPlayer,
        sender: Account,
        amount: BigDecimal,
        currency: Currency,
        receiver: Account,
        ignoreSenderMinimum: Boolean,
        ignoreReceiverMinimum: Boolean,
        additionalSenderData: ObjectSet<TransactionData>,
        additionalReceiverData: ObjectSet<TransactionData>
    ) = transactionService.transfer(
        initiator,
        sender.accountId,
        amount,
        currency,
        receiver.accountId,
        ignoreSenderMinimum,
        ignoreReceiverMinimum,
        additionalSenderData,
        additionalReceiverData
    )

    override suspend fun balance(
        account: Account,
        currency: Currency
    ) = transactionService.balanceDecimal(
        account.accountId,
        currency
    )
}