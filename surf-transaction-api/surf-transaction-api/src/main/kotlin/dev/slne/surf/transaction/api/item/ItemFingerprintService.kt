package dev.slne.surf.transaction.api.item

import dev.slne.surf.surfapi.core.api.util.requiredService
import dev.slne.surf.transaction.api.util.InternalTransactionApi

/**
 * Internal service for managing item fingerprints.
 *
 * Responsible for persisting and resolving [ItemFingerprint] instances.
 */
@InternalTransactionApi
interface ItemFingerprintService {

    /**
     * Finds an existing fingerprint by its [hash], or `null` if none exists.
     */
    suspend fun findByHash(hash: String): ItemFingerprint?

    /**
     * Persists a fingerprint if it does not yet exist.
     * Returns the existing or newly created fingerprint.
     */
    suspend fun findOrCreate(fingerprint: ItemFingerprint): ItemFingerprint

    companion object {
        val instance = requiredService<ItemFingerprintService>()
    }
}