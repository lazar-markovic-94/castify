package com.castify.domain.talent

import java.math.BigDecimal
import java.time.Instant
import java.util.*

data class TalentProfile(
    val talentProfileId: UUID,
    val userId: UUID,
    val fullName: String,
    val bio: String? = null,
    val location: Location,
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
    }

    fun updateBio(newBio: String?): TalentProfile = copy(bio = newBio, updatedAt = Instant.now())

    fun updateLocation(newLocation: Location): TalentProfile = copy(location = newLocation, updatedAt = Instant.now())

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

data class Location(
    val mode: LocationMode,
    val latitude: BigDecimal? = null,
    val longitude: BigDecimal? = null,
    val city: String? = null,
    val country: String? = null
) {
    init {
        if (mode == LocationMode.SPECIFIC) {
            require(latitude != null && longitude != null) {
                "Latitude and longitude are required for specific location mode"
            }
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
