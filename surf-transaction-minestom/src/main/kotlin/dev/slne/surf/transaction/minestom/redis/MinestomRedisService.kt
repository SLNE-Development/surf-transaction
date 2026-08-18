package dev.slne.surf.transaction.minestom.redis

import com.google.auto.service.AutoService
import dev.slne.surf.transaction.core.client.pay.PaymentEventsHandler
import dev.slne.surf.transaction.core.client.redis.RedisService
import dev.slne.surf.transaction.core.client.transaction.TransactionEventsHandler

@AutoService(RedisService::class)
class MinestomRedisService : RedisService() {

    override fun register() {
        super.register()

        redisApi.subscribeToEvents(PaymentEventsHandler())
        redisApi.subscribeToEvents(TransactionEventsHandler())
    }
}
