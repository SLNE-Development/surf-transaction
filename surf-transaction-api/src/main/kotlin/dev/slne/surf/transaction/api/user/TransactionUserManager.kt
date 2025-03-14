package dev.slne.surf.transaction.api.user

import net.kyori.adventure.util.Services
import java.util.*

interface TransactionUserManager {

    operator fun get(uuid: UUID): TransactionUser

    companion object {
        val INSTANCE =
            Services.serviceWithFallback(TransactionUserManager::class.java).orElseThrow {
                IllegalStateException("No TransactionUserManager implementation found")
            }
    }

}

val transactionUserManager: TransactionUserManager
    get() = TransactionUserManager.INSTANCE