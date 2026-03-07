package dev.slne.surf.transaction.paper.item

import dev.slne.surf.transaction.api.item.ItemFingerprint
import net.kyori.adventure.key.Key

data class PaperItemFingerprint(
    override val itemType: Key,
    override val componentData: String?,
    override val hash: String,
    override val serializedTemplate: ByteArray,
    override val dataVersion: Int
) : ItemFingerprint {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PaperItemFingerprint

        return hash == other.hash
    }

    override fun hashCode(): Int {
        return hash.hashCode()
    }

    override fun toString(): String {
        return "PaperItemFingerprint(itemType=$itemType, hash='$hash')"
    }
}
