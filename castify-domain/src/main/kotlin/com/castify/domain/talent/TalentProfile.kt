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
    val socialLinks: Set<SocialLink> = emptySet(),
    val verificationStatus: VerificationStatus = VerificationStatus.PENDING,
    val profileImageUrl: String? = null,
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

    fun updateTags(newTags: Set<String>): TalentProfile = copy(tags = newTags, updatedAt = Instant.now())

    fun addTag(tag: String): TalentProfile = copy(tags = tags + tag, updatedAt = Instant.now())

    fun removeTag(tag: String): TalentProfile = copy(tags = tags - tag, updatedAt = Instant.now())

    fun updateCapabilities(newCapabilities: TalentCapabilities): TalentProfile =
        copy(capabilities = newCapabilities, updatedAt = Instant.now())

    fun addSocialLink(link: SocialLink): TalentProfile = copy(socialLinks = socialLinks + link, updatedAt = Instant.now())

    fun removeSocialLink(platform: String): TalentProfile =
        copy(socialLinks = socialLinks.filterNot { it.platform == platform }.toSet(), updatedAt = Instant.now())

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

data class SocialLink(
    val platform: String,
    val url: String,
    val followerCount: Long? = null
) {
    init {
        require(platform.isNotBlank()) { "Platform cannot be blank" }
        require(url.isNotBlank()) { "URL cannot be blank" }
        require(url.matches(Regex("^https?://.*"))) { "URL must start with http:// or https://" }
    }
}

enum class VerificationStatus {
    PENDING,
    VERIFIED,
    REJECTED
}
