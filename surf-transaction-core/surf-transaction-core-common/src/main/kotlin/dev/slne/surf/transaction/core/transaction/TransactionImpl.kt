package dev.slne.surf.transaction.core.transaction

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.player.toOfflineCloudPlayer
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
    val senderUuid: @Contextual UUID?,
    val receiverUuid: @Contextual UUID?,
    val currencyName: String,
    override val amount: @Contextual BigDecimal,
    override val ignoreMinimumAmount: Boolean = false,
    override val data: Set<TransactionData> = emptySet()
) : Transaction {
    override val sender: OfflineCloudPlayer? get() = senderUuid.toOfflineCloudPlayer()
    override val receiver: OfflineCloudPlayer? get() = receiverUuid.toOfflineCloudPlayer()
    override val currency: Currency
        get() = Currency.byName(currencyName)
            ?: error("Currency with name '$currencyName' not found. Ensure the currency is registered in the system.")
}