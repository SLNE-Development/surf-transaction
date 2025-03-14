package dev.slne.surf.transaction.core.user

import com.github.benmanes.caffeine.cache.Caffeine
import com.sksamuel.aedile.core.expireAfterAccess
import dev.slne.surf.transaction.api.user.TransactionUser
import java.util.*
import kotlin.time.Duration.Companion.minutes

object TransactionUserManager {

    private val users = Caffeine.newBuilder()
        .expireAfterAccess(30.minutes)
        .build<UUID, TransactionUser> {
            CoreTransactionUser(it)
        }

    /**
     * Get a user by their UUID
     *
     * @param uuid The UUID of the user
     * @return The user
     */
    fun get(uuid: UUID): TransactionUser = users[uuid]
}