package dev.slne.surf.transaction.fallback

import com.google.auto.service.AutoService
import dev.slne.surf.transaction.api.TransactionApi
import dev.slne.surf.transaction.core.currency.currencyService
import dev.slne.surf.transaction.core.user.TransactionUserManager
import net.kyori.adventure.util.Services.Fallback
import java.util.*

@AutoService(TransactionApi::class)
class FallbackTransactionApi : TransactionApi, Fallback {
    override fun getCurrencies() = currencyService.currencies

    override fun getCurrencyByName(name: String) = currencyService.getCurrencyByName(name)

    override fun getTransactionUser(uuid: UUID) = TransactionUserManager.get(uuid)
}