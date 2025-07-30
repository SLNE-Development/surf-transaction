package dev.slne.surf.transaction.core.transaction

import dev.slne.surf.cloud.api.common.player.OfflineCloudPlayer
import dev.slne.surf.cloud.api.common.player.toOfflineCloudPlayer
import dev.slne.surf.surfapi.core.api.util.mutableObjectSetOf
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.Transaction
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import it.unimi.dsi.fastutil.objects.ObjectSet
import java.math.BigDecimal
import java.util.*

data class TransactionImpl(
    override val identifier: UUID,
    val senderUuid: UUID?,
    val receiverUuid: UUID?,
    val currencyName: String,
    override val amount: BigDecimal,
    override val ignoreMinimumAmount: Boolean = false,
    override val data: ObjectSet<TransactionData> = mutableObjectSetOf<TransactionData>()
) : Transaction {
    override val sender: OfflineCloudPlayer? get() = senderUuid.toOfflineCloudPlayer()
    override val receiver: OfflineCloudPlayer? get() = receiverUuid.toOfflineCloudPlayer()
    override val currency: Currency
        get() = Currency.byName(currencyName)
            ?: error("Currency with name '$currencyName' not found. Ensure the currency is registered in the system.")
}