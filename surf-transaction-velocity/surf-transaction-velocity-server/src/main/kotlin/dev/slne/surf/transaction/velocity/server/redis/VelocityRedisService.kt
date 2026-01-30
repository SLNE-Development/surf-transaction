package dev.slne.surf.transaction.velocity.server.redis

import com.google.auto.service.AutoService
import dev.slne.surf.transaction.core.redis.RedisService

@AutoService(RedisService::class)
class VelocityRedisService : RedisService()