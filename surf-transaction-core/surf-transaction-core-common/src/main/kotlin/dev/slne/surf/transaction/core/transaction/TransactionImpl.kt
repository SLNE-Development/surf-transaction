package dev.slne.surf.transaction.core.transaction

import dev.slne.surf.surfapi.core.api.serializer.java.number.bigdecimal.SerializableBigDecimal
import dev.slne.surf.surfapi.core.api.serializer.java.uuid.SerializableStringUUID
import dev.slne.surf.transaction.api.account.Account
import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.api.transaction.Transaction
import dev.slne.surf.transaction.api.transaction.data.TransactionData
import kotlinx.serialization.Serializable
import org.jetbrains.annotations.Unmodifiable
import java.math.BigDecimal
import java.util.*

@Serializable
class TransactionImpl(
    override val identifier: SerializableStringUUID,
    override val initiator: SerializableStringUUID?,
    override val senderAccountId: SerializableStringUUID?,
    override val receiverAccountId: SerializableStringUUID?,
    val currencyName: String,
    override val amount: SerializableBigDecimal,
    override val data: @Unmodifiable Set<TransactionData>,
    override val ignoreMinimumAmount: Boolean = false
) : Transaction {
    override val currency: Currency
        get() = Currency.byName(currencyName)
            ?: error("Currency with name '$currencyName' not found. Ensure the currency is registered in the system.")

    override suspend fun senderAccount() = senderAccountId?.let { Account.byId(it) }
    override suspend fun receiverAccount() = receiverAccountId?.let { Account.byId(it) }

    override fun toString(): String {
        return "TransactionImpl(identifier=$identifier, initiator=$initiator, senderAccountId=$senderAccountId, receiverAccountId=$receiverAccountId, currencyName='$currencyName', amount=$amount, data=$data, ignoreMinimumAmount=$ignoreMinimumAmount)"
    }
}
