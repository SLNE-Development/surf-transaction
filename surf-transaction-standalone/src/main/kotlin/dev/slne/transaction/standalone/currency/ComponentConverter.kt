package dev.slne.transaction.standalone.currency

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer

@Converter(autoApply = true)
class ComponentConverter : AttributeConverter<Component, String> {

    @Suppress("NAME_SHADOWING")
    override fun convertToDatabaseColumn(attribute: Component?): String {
        val attribute = attribute ?: return ""

        return GsonComponentSerializer.gson().serialize(attribute)
    }

    @Suppress("NAME_SHADOWING")
    override fun convertToEntityAttribute(dbData: String?): Component {
        val dbData = dbData ?: return Component.empty()

        return GsonComponentSerializer.gson().deserialize(dbData)
    }
}