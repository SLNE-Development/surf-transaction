package dev.slne.surf.transaction.core.transaction

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.Transaction
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.math.BigDecimal
import java.util.*

@Serializable
data class TransactionImpl(
    override val identifier: @Contextual UUID,
    override val initiator: OfflineCloudPlayer?,
    override val senderAccountId: @Contextual UUID?,
    override val receiverAccountId: @Contextual UUID?,
    val currencyName: String,
    override val amount: @Contextual BigDecimal,
    override val ignoreMinimumAmount: Boolean = false,
    override val data: Set<TransactionData> = emptySet()
) : Transaction {

    override suspend fun sender() = Account[senderAccountId]
    override suspend fun receiver() = Account[receiverAccountId]

    override val currency: Currency
        get() = Currency.byName(currencyName)
            ?: error("Currency with name '$currencyName' not found. Ensure the currency is registered in the system.")
}