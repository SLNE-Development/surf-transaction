package dev.slne.surf.transaction.core.redis

import dev.slne.surf.redis.RedisApi
import dev.slne.surf.redis.event.RedisEvent
import dev.slne.surf.surfapi.core.api.util.requiredService
import dev.slne.surf.transaction.core.currency.CurrencyEventsListener
import org.jetbrains.annotations.Blocking
import org.jetbrains.annotations.MustBeInvokedByOverriders
import kotlin.time.Duration

abstract class RedisService {
    val redisApi = RedisApi.create()

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

        inline fun <K : Any, reified V : Any> cache(
            namespace: String,
            ttl: Duration,
            noinline keyToString: (K) -> String = { it.toString() }
        ) = get().redisApi.createSimpleCache<K, V>(
            "surf-transaction:$namespace",
            ttl,
            keyToString
        )
    }
}