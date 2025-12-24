package com.castify.domain.talent

import java.math.BigDecimal
import java.time.Instant
import java.util.*

data class TalentProfile(
    val talentProfileId: UUID,
    val userId: UUID,
    val fullName: String,
    val bio: String? = null,
    val locationMode: LocationMode = LocationMode.SPECIFIC,
    val primaryLocation: PrimaryLocation? = null,
    val geoAreas: Set<GeoArea> = emptySet(),
    val languages: Set<String> = emptySet(),
    val tags: Set<String> = emptySet(),
    val capabilities: TalentCapabilities = TalentCapabilities(),
    val verificationStatus: VerificationStatus = VerificationStatus.PENDING,
    val profileImageUrl: String? = null,
    val introductionVideoUrl: String? = null,
    val introductionVideoDuration: Int? = null, // Duration in seconds
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now()
) {
    init {
        require(fullName.isNotBlank()) { "Full name cannot be blank" }
        if (locationMode == LocationMode.SPECIFIC || locationMode == LocationMode.HYBRID) {
            require(primaryLocation != null) {
                "Primary location is required for SPECIFIC and HYBRID location modes"
            }
        }
        require(geoAreas.count { it.isPrimary } <= 1) {
            "Only one geo area can be marked as primary"
        }
        // Validate introduction video duration if provided
        if (introductionVideoDuration != null) {
            require(introductionVideoDuration > 0) {
                "Introduction video duration must be positive"
            }
            require(introductionVideoDuration <= 30) {
                "Introduction video must be 30 seconds or less"
            }
        }
        // If duration is provided, URL must also be provided
        if (introductionVideoDuration != null) {
            require(!introductionVideoUrl.isNullOrBlank()) {
                "Introduction video URL is required when duration is provided"
            }
        }
    }

    fun updateBio(newBio: String?): TalentProfile = copy(bio = newBio, updatedAt = Instant.now())

    fun updateLocationMode(newMode: LocationMode): TalentProfile = copy(locationMode = newMode, updatedAt = Instant.now())

    fun updatePrimaryLocation(newLocation: PrimaryLocation): TalentProfile =
        copy(primaryLocation = newLocation, updatedAt = Instant.now())

    fun addGeoArea(geoArea: GeoArea): TalentProfile {
        require(!geoAreas.any { it.city == geoArea.city && it.country == geoArea.country }) {
            "Geo area ${geoArea.city}, ${geoArea.country} already exists"
        }
        val updatedAreas = if (geoArea.isPrimary) {
            geoAreas.map { it.copy(isPrimary = false) }.toSet() + geoArea
        } else {
            geoAreas + geoArea
        }
        return copy(geoAreas = updatedAreas, updatedAt = Instant.now())
    }

    fun removeGeoArea(city: String, country: String): TalentProfile =
        copy(geoAreas = geoAreas.filterNot { it.city == city && it.country == country }.toSet(), updatedAt = Instant.now())

    fun updateGeoAreas(newGeoAreas: Set<GeoArea>): TalentProfile {
        require(newGeoAreas.count { it.isPrimary } <= 1) {
            "Only one geo area can be marked as primary"
        }
        return copy(geoAreas = newGeoAreas, updatedAt = Instant.now())
    }

    fun updateLanguages(newLanguages: Set<String>): TalentProfile = copy(languages = newLanguages, updatedAt = Instant.now())

    fun addLanguage(language: String): TalentProfile = copy(languages = languages + language, updatedAt = Instant.now())

    fun removeLanguage(language: String): TalentProfile = copy(languages = languages - language, updatedAt = Instant.now())

    fun updateTags(newTags: Set<String>): TalentProfile {
        // Validate all tags are from predefined list
        val invalidTags = newTags.filterNot { PredefinedTags.isValidTag(it) }
        require(invalidTags.isEmpty()) {
            "Invalid tags: ${invalidTags.joinToString(", ")}. Valid tags are: ${PredefinedTags.getAllValidTags().joinToString(", ")}"
        }
        return copy(tags = newTags, updatedAt = Instant.now())
    }

    fun addTag(tag: String): TalentProfile {
        require(PredefinedTags.isValidTag(tag)) {
            "Invalid tag: $tag. Valid tags are: ${PredefinedTags.getAllValidTags().joinToString(", ")}"
        }
        return copy(tags = tags + tag, updatedAt = Instant.now())
    }

    fun removeTag(tag: String): TalentProfile = copy(tags = tags - tag, updatedAt = Instant.now())

    fun getTagsByCategory(category: TagCategory): Set<String> {
        return tags.filter { PredefinedTags.getCategoryForTag(it) == category }.toSet()
    }

    fun getPrimaryTags(): Set<String> = getTagsByCategory(TagCategory.PRIMARY)

    fun getSecondaryTags(): Set<String> = getTagsByCategory(TagCategory.SECONDARY)

    fun updateCapabilities(newCapabilities: TalentCapabilities): TalentProfile =
        copy(capabilities = newCapabilities, updatedAt = Instant.now())

    fun updateIntroductionVideo(videoUrl: String?, duration: Int?): TalentProfile {
        // Validate that both are provided together or both are null
        if (videoUrl != null || duration != null) {
            require(!videoUrl.isNullOrBlank()) { "Introduction video URL cannot be blank" }
            require(duration != null && duration > 0) { "Introduction video duration must be positive" }
            require(duration <= 30) { "Introduction video must be 30 seconds or less" }
        }
        return copy(
            introductionVideoUrl = videoUrl,
            introductionVideoDuration = duration,
            updatedAt = Instant.now()
        )
    }

    fun verify(): TalentProfile = copy(verificationStatus = VerificationStatus.VERIFIED, updatedAt = Instant.now())

    fun reject(): TalentProfile = copy(verificationStatus = VerificationStatus.REJECTED, updatedAt = Instant.now())

    fun isVerified(): Boolean = verificationStatus == VerificationStatus.VERIFIED
}

/**
 * Represents a geographical area where talent provides services.
 * Multiple geo areas can be specified to indicate coverage across different cities.
 */
data class GeoArea(
    val city: String,
    val country: String,
    val isPrimary: Boolean = false
) {
    init {
        require(city.isNotBlank()) { "City cannot be blank" }
        require(country.isNotBlank()) { "Country cannot be blank" }
    }
}

/**
 * Primary location with coordinates for distance-based search.
 * Represents the main location where talent is based.
 */
data class PrimaryLocation(
    val latitude: BigDecimal,
    val longitude: BigDecimal,
    val city: String,
    val country: String
) {
    init {
        require(city.isNotBlank()) { "City cannot be blank" }
        require(country.isNotBlank()) { "Country cannot be blank" }
        require(latitude >= BigDecimal("-90") && latitude <= BigDecimal("90")) {
            "Latitude must be between -90 and 90"
        }
        require(longitude >= BigDecimal("-180") && longitude <= BigDecimal("180")) {
            "Longitude must be between -180 and 180"
        }
    }
}

enum class LocationMode {
    REMOTE,
    SPECIFIC,
    HYBRID
}

data class TalentCapabilities(
    val canDoVoiceover: Boolean = false,
    val canDoActing: Boolean = false,
    val canDoModeling: Boolean = false,
    val canDoSinging: Boolean = false,
    val canDoDancing: Boolean = false,
    val canDoHosting: Boolean = false,
    val otherCapabilities: String? = null
) {
    fun hasAnyCapability(): Boolean =
        canDoVoiceover || canDoActing || canDoModeling || canDoSinging || canDoDancing || canDoHosting
}

enum class VerificationStatus {
    PENDING,
    VERIFIED,
    REJECTED
}
