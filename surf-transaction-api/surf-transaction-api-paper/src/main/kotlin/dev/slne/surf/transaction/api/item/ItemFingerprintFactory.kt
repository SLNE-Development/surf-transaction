package dev.slne.surf.transaction.api.item

import dev.slne.surf.surfapi.core.api.util.requiredService
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemType

private val INSTANCE = requiredService<ItemFingerprintFactory>()

interface ItemFingerprintFactory {

    fun create(type: ItemType): ItemFingerprint
    fun create(itemStack: ItemStack): ItemFingerprint

    fun matches(itemStack: ItemStack, fingerprint: ItemFingerprint): Boolean

    fun reconstruct(fingerprint: ItemFingerprint, amount: Int = 1): ItemStack


    companion object : ItemFingerprintFactory by INSTANCE {
        val instance = INSTANCE
    }
}