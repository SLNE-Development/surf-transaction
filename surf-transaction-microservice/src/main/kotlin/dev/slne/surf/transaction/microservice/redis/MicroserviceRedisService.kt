package dev.slne.surf.transaction.microservice.redis

import com.google.auto.service.AutoService
import dev.slne.surf.transaction.core.db.account.AccountRepository
import dev.slne.surf.transaction.core.db.currency.CurrencyRepository
import dev.slne.surf.transaction.core.db.transaction.TransactionRepository
import dev.slne.surf.transaction.core.redis.RedisService

@AutoService(RedisService::class)
class MicroserviceRedisService : RedisService() {
    override fun register() {
        super.register()

        CurrencyRepository.init()
        AccountRepository.init()
        TransactionRepository.init()
    }
}
