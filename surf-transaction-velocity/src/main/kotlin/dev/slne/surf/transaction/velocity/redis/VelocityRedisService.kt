package dev.slne.surf.transaction.velocity.redis

import com.google.auto.service.AutoService
import dev.slne.surf.transaction.core.client.redis.RedisService

@AutoService(RedisService::class)
class VelocityRedisService : RedisService()