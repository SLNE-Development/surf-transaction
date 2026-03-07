package dev.slne.surf.transaction.paper.server.redis

import com.google.auto.service.AutoService
import dev.slne.surf.transaction.core.redis.RedisService
import dev.slne.surf.transaction.paper.server.pay.PaymentEventsHandler
import dev.slne.surf.transaction.paper.server.transaction.TransactionEventsHandler

@AutoService(RedisService::class)
class PaperRedisService : RedisService() {

    override fun register() {
        super.register()

        redisApi.subscribeToEvents(PaymentEventsHandler())
        redisApi.subscribeToEvents(TransactionEventsHandler())
    }
}