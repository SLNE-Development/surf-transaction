package dev.slne.surf.transaction.core.user

import com.google.auto.service.AutoService
import dev.slne.surf.transaction.api.user.TransactionUserService
import java.util.*

@AutoService(TransactionUserService::class)
class TransactionUserServiceImpl : TransactionUserService {
    /* Currently not worth caching
    private val cache = Caffeine.newBuilder()
        .maximumSize(10_000)
        .expireAfterAccess(10, TimeUnit.MINUTES)
        .build<UUID, TransactionUserImpl> { uuid ->
            TransactionUserImpl(uuid)
        }
     */

    override fun byUuid(uuid: UUID) = TransactionUserImpl(uuid)
}