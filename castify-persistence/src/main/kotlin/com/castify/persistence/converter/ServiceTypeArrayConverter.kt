package com.castify.persistence.converter

import com.castify.domain.collaboration.ServiceType
import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import org.slf4j.LoggerFactory

/**
 * JPA converter for PostgreSQL TEXT[] arrays.
 * Converts between List<ServiceType> and PostgreSQL text array.
 *
 * Note: Not using autoApply to have explicit control over which fields use this converter.
 */
@Converter
class ServiceTypeArrayConverter : AttributeConverter<List<ServiceType>, Array<String>> {

    private val logger = LoggerFactory.getLogger(ServiceTypeArrayConverter::class.java)

    override fun convertToDatabaseColumn(attribute: List<ServiceType>?): Array<String> {
        // Return empty array for null, but validation in domain ensures we never save empty lists
        if (attribute.isNullOrEmpty()) {
            logger.warn("Attempting to convert null or empty ServiceType list to database column")
            return emptyArray()
        }

        return attribute.map { it.name }.toTypedArray()
    }

    override fun convertToEntityAttribute(dbData: Array<String>?): List<ServiceType> {
        if (dbData.isNullOrEmpty()) {
            return emptyList()
        }

        return try {
            dbData.map { value ->
                try {
                    ServiceType.valueOf(value)
                } catch (e: IllegalArgumentException) {
                    logger.error("Invalid ServiceType value in database: $value", e)
                    throw IllegalStateException("Invalid service type in database: $value. Expected one of: ${ServiceType.entries.joinToString()}", e)
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to convert database array to ServiceType list: ${dbData.contentToString()}", e)
            throw e
        }
    }
}
