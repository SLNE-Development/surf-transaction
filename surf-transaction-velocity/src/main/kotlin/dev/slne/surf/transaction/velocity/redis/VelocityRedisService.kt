package dev.slne.surf.transaction.velocity.redis

import com.google.auto.service.AutoService
import dev.slne.surf.transaction.core.common.redis.RedisService

@AutoService(RedisService::class)
class VelocityRedisService : RedisService()