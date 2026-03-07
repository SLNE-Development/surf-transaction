package dev.slne.surf.transaction.api.item

import dev.slne.surf.transaction.api.util.InternalTransactionApi
import net.kyori.adventure.key.Key
import org.jetbrains.annotations.ApiStatus
import java.util.*

/**
 * A canonical, version-neutral identifier for a specific item type
 * including all relevant metadata (enchantments, components, etc.).
 *
 * Two items are considered the "same type" if and only if their
 * [ItemFingerprint] instances are equal.
 *
 * Implementations are responsible for constructing fingerprints from
 * platform-specific item representations (e.g. Paper ItemStack).
 */
@ApiStatus.NonExtendable
interface ItemFingerprint {

    /**
     * The namespaced item type key (e.g. `"minecraft:diamond_sword"`).
     */
    val itemType: Key

    /**
     * A canonical, sorted JSON representation of all identity-relevant
     * data components (enchantments, custom name, lore, damage, etc.).
     *
     * `null` if the item has no special components beyond its type.
     */
    val componentData: String?

    /**
     * A SHA-256 hash of [itemType] and [componentData] combined.
     *
     * Used as a stable, fixed-length database key for efficient lookups.
     */
    val hash: String

    /**
     * The serialized bytes of a single-count template item.
     *
     * This is platform-specific and may change across server versions.
     * It is used to reconstruct the actual item on the platform where
     * it was last stored or migrated.
     */
    @InternalTransactionApi
    val serializedTemplate: ByteArray

    /**
     * The Minecraft data version this fingerprint's [serializedTemplate]
     * was created with.
     */
    @InternalTransactionApi
    val dataVersion: Int
}