package dev.slne.surf.transaction.core.client.platform

import dev.slne.surf.api.core.util.requiredService
import net.kyori.adventure.audience.Audience
import java.util.UUID

/**
 * The handful of server-specific operations the platform-neutral client code depends on.
 */
interface TransactionPlatform {

    /**
     * Runs [block] for the player [uuid] belongs to, on whichever context that platform requires
     * for sending them messages, and does nothing when they are not online.
     */
    fun withOnlinePlayer(uuid: UUID, block: suspend (Audience) -> Unit)
}

val transactionPlatform: TransactionPlatform get() = instance

private val instance = requiredService<TransactionPlatform>()
