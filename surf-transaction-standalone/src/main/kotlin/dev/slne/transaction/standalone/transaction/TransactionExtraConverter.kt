package dev.slne.transaction.standalone.transaction

import dev.slne.transaction.standalone.gson
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter

@Converter(autoApply = false)
class TransactionExtraConverter: AttributeConverter<Map<String, String>, String> {

    @Suppress("NAME_SHADOWING")
    override fun convertToDatabaseColumn(attribute: Map<String, String>?): String {
        val attribute = attribute ?: return ""

        return try {
            gson.toJson(attribute)
        } catch (e: Exception) {
            ""
        }
    }

    @Suppress("UNCHECKED_CAST", "NAME_SHADOWING")
    override fun convertToEntityAttribute(dbData: String?): Map<String, String> {
        val dbData = dbData ?: return emptyMap()

        return try {
            gson.fromJson(dbData, Map::class.java) as Map<String, String>
        } catch (e: Exception) {
            emptyMap()
        }
    }
}