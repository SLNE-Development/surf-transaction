package dev.slne.surf.transaction.fallback

import com.google.auto.service.AutoService
import dev.slne.surf.transaction.api.InternalTransactionApiBridge
import net.kyori.adventure.util.Services.Fallback
import java.util.*

@AutoService(InternalTransactionApiBridge::class)
class FallbackTransactionApi : InternalTransactionApiBridge, Fallback {
    override val defaultCurrency get() = CurrencyService.defaultCurrency
    override val currencies get() = CurrencyService.currencies
    override fun getCurrencyByName(name: String) = CurrencyService.getCurrencyByName(name)
    override fun getTransactionUser(uuid: UUID) = TransactionUserManager.get(uuid)
}