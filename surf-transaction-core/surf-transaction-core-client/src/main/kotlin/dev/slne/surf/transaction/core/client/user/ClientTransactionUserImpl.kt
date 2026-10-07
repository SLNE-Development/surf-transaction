package dev.slne.surf.transaction.core.client.user

import dev.slne.surf.transaction.api.currency.Currency
import dev.slne.surf.transaction.core.client.balance.BalanceCache
import dev.slne.surf.transaction.core.common.user.TransactionUserImpl
import java.util.*

class ClientTransactionUserImpl(userUuid: UUID) : TransactionUserImpl(userUuid) {
    override fun cachedBalance(currency: Currency) = BalanceCache.cachedBalance(userUuid, currency)
}
