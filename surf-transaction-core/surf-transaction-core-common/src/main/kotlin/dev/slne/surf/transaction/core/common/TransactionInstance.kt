package dev.slne.surf.transaction.core.common

import dev.slne.surf.api.core.util.requiredService
import org.jetbrains.annotations.MustBeInvokedByOverriders

abstract class TransactionInstance {
    @MustBeInvokedByOverriders
    open suspend fun load() {
    }

    @MustBeInvokedByOverriders
    open suspend fun enable() {
    }

    @MustBeInvokedByOverriders
    open suspend fun disable() {
    }

    companion object {
        val INSTANCE get() = instance
    }
}

private val instance = requiredService<TransactionInstance>()
