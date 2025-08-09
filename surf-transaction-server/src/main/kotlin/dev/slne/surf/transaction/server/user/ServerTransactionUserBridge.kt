package dev.slne.surf.transaction.server.user

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.TransactionResult
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.user.InternalTransactionUserBridge
import dev.slne.surf.transaction.server.transaction.TransactionService
import it.unimi.dsi.fastutil.objects.ObjectSet
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class ServerTransactionUserBridge(
    private val transactionService: TransactionService,
) : InternalTransactionUserBridge {

    override suspend fun deposit(
        account: Account,
        initiator: OfflineCloudPlayer?,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult =
        transactionService.deposit(
            initiator,
            account.accountId,
            amount,
            currency,
            ignoreMinimum,
            *additionalData
        )

    override suspend fun withdraw(
        account: Account,
        initiator: OfflineCloudPlayer?,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ): TransactionResult =
        transactionService.withdraw(
            initiator,
            account.accountId,
            amount,
            currency,
            ignoreMinimum,
            *additionalData
        )

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
    ): TransactionResult = transactionService.transfer(
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

    override suspend fun balanceDecimal(
        account: Account,
        currency: Currency
    ): BigDecimal = transactionService.balanceDecimal(account, currency)
}