package dev.slne.surf.transaction.api.user

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.util.objectSetOf
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.account.HasAccounts
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import dev.slne.surf.transaction.api.util.InternalTransactionApi
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.math.BigDecimal

interface TransactionUser : HasTransactions, HasAccounts {

    override val cloudPlayer: OfflineCloudPlayer

    override suspend fun deposit(
        account: Account,
        initiator: OfflineCloudPlayer,
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean,
        vararg additionalData: TransactionData
    ) = getDefaultAccount().deposit(
        account,
        initiator,
        amount,
        currency,
        ignoreMinimum,
        *additionalData
    )

    suspend fun deposit(
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean = false,
        vararg additionalData: TransactionData
    ) = deposit(
        getDefaultAccount(),
        cloudPlayer,
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
    ) = getDefaultAccount().withdraw(
        account,
        initiator,
        amount,
        currency,
        ignoreMinimum,
        *additionalData
    )

    suspend fun withdraw(
        amount: BigDecimal,
        currency: Currency,
        ignoreMinimum: Boolean = false,
        vararg additionalData: TransactionData
    ) = withdraw(
        getDefaultAccount(),
        cloudPlayer,
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
    ) = getDefaultAccount().transfer(
        initiator,
        sender,
        amount,
        currency,
        receiver,
        ignoreSenderMinimum,
        ignoreReceiverMinimum,
        additionalSenderData,
        additionalReceiverData
    )

    suspend fun transfer(
        amount: BigDecimal,
        currency: Currency,
        receiver: Account,
        ignoreSenderMinimum: Boolean = false,
        ignoreReceiverMinimum: Boolean = false,
        additionalSenderData: ObjectSet<TransactionData> = objectSetOf(),
        additionalReceiverData: ObjectSet<TransactionData> = objectSetOf()
    ) =
        transfer(
            cloudPlayer,
            getDefaultAccount(),
            amount,
            currency,
            receiver,
            ignoreSenderMinimum,
            ignoreReceiverMinimum,
            additionalSenderData,
            additionalReceiverData
        )

    override suspend fun balance(
        account: Account,
        currency: Currency
    ) = getDefaultAccount().balance(account, currency)

    suspend fun balance(
        currency: Currency
    ): BigDecimal = balance(getDefaultAccount(), currency)

    @OptIn(InternalTransactionApi::class)
    companion object {
        operator fun invoke(cloudPlayer: OfflineCloudPlayer): TransactionUser =
            TransactionUserImpl(cloudPlayer)

        internal class TransactionUserImpl(
            override val cloudPlayer: OfflineCloudPlayer
        ) : TransactionUser
    }

}

fun OfflineCloudPlayer.transactionUser() = TransactionUser(this)