package dev.slne.surf.transaction.core.redis

import dev.slne.surf.redis.RedisApi
import dev.slne.surf.redis.event.RedisEvent
import dev.slne.surf.surfapi.core.api.util.requiredService
import dev.slne.surf.transaction.core.TransactionInstance
import dev.slne.surf.transaction.core.currency.CurrencyEventsListener
import org.jetbrains.annotations.Blocking
import org.jetbrains.annotations.MustBeInvokedByOverriders

abstract class RedisService {
    val redisApi = RedisApi.create(TransactionInstance.get().dataPath)

    @Blocking
    fun connect() {
        register()
        redisApi.freezeAndConnect()
    }

    @Blocking
    fun disconnect() {
        redisApi.disconnect()
    }

    @MustBeInvokedByOverriders
    protected open fun register() {
        redisApi.subscribeToEvents(CurrencyEventsListener())
    }

    companion object {
        val instance = requiredService<RedisService>()
        fun get() = instance

        fun publish(event: RedisEvent) = get().redisApi.publishEvent(event)
    }
}