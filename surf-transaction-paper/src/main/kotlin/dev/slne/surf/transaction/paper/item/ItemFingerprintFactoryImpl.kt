package dev.slne.surf.transaction.paper.item

import com.google.auto.service.AutoService
import com.google.common.hash.Hashing
import dev.slne.surf.surfapi.bukkit.api.extensions.server
import dev.slne.surf.transaction.api.item.ItemFingerprint
import dev.slne.surf.transaction.api.item.ItemFingerprintFactory
import io.papermc.paper.datacomponent.DataComponentTypes
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import org.bukkit.Bukkit
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ItemType

@AutoService(ItemFingerprintFactory::class)
class ItemFingerprintFactoryImpl: ItemFingerprintFactory {
    override fun create(type: ItemType): ItemFingerprint {
        return create(type.createItemStack())
    }

    override fun create(itemStack: ItemStack): ItemFingerprint {
        val type = itemStack.type.asItemType() ?: error("ItemStack does not have a valid ItemType")
        val typeKey = type.key()
        val componentData = extractCanonicalComponents(itemStack)

        val hashInput = "$typeKey|${componentData.orEmpty()}"
        val hash = Hashing.sha256().hashString(hashInput, Charsets.UTF_8).toString()

        val template = itemStack.clone().apply { amount = 1 }
        val serializedTemplate = template.serializeAsBytes()

        @Suppress("DEPRECATION")
        val dataVersion = Bukkit.getUnsafe().dataVersion

        return PaperItemFingerprint(
            typeKey,
            componentData,
            hash,
            serializedTemplate,
            dataVersion
        )
    }

    override fun matches(
        itemStack: ItemStack,
        fingerprint: ItemFingerprint
    ): Boolean {
        val computedFingerprint = create(itemStack)
        return fingerprint == computedFingerprint
    }

    override fun reconstruct(
        fingerprint: ItemFingerprint,
        amount: Int
    ): ItemStack {
        val item = ItemStack.deserializeBytes(fingerprint.serializedTemplate)
        item.amount = amount
        return item
    }
}