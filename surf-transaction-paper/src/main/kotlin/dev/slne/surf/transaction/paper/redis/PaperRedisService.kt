package dev.slne.surf.transaction.paper.redis

import com.google.auto.service.AutoService
import dev.slne.surf.transaction.core.redis.RedisService
import dev.slne.surf.transaction.paper.pay.PaymentEventsHandler
import dev.slne.surf.transaction.paper.transaction.TransactionEventsHandler

@AutoService(RedisService::class)
class PaperRedisService : RedisService() {

    override fun register() {
        super.register()

        redisApi.subscribeToEvents(PaymentEventsHandler())
        redisApi.subscribeToEvents(TransactionEventsHandler())
    }
}