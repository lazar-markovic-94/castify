package com.castify.usecase.dto

import com.castify.domain.talent.*
import com.castify.domain.rating.TalentTier
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal
import java.time.Instant
import java.util.*

data class CreateTalentProfileRequest(
    @field:NotBlank(message = "Full name is required")
    val fullName: String?,
    val bio: String? = null,
    val location: LocationDto?,
    val profileImageUrl: String? = null
)

data class UpdateTalentProfileRequest(
    val fullName: String? = null,
    val bio: String? = null,
    val profileImageUrl: String? = null
)

data class LocationDto(
    val mode: LocationMode?,
    val latitude: BigDecimal? = null,
    val longitude: BigDecimal? = null,
    val city: String? = null,
    val country: String? = null
) {
    fun toDomain(): Location {
        val locationMode = requireNotNull(mode) { "Location mode is required" }
        return Location(
            mode = locationMode,
            latitude = latitude,
            longitude = longitude,
            city = city,
            country = country
        )
    }

    companion object {
        fun fromDomain(location: Location): LocationDto = LocationDto(
            mode = location.mode,
            latitude = location.latitude,
            longitude = location.longitude,
            city = location.city,
            country = location.country
        )
    }
}

data class UpdateLocationRequest(
    val location: LocationDto?
)

data class UpdateLanguagesRequest(
    val languages: Set<String>?
)

data class UpdateTagsRequest(
    val tags: Set<String>?
)

data class UpdateCapabilitiesRequest(
    val canDoVoiceover: Boolean = false,
    val canDoActing: Boolean = false,
    val canDoModeling: Boolean = false,
    val canDoSinging: Boolean = false,
    val canDoDancing: Boolean = false,
    val canDoHosting: Boolean = false,
    val otherCapabilities: String? = null
) {
    fun toDomain(): TalentCapabilities = TalentCapabilities(
        canDoVoiceover = canDoVoiceover,
        canDoActing = canDoActing,
        canDoModeling = canDoModeling,
        canDoSinging = canDoSinging,
        canDoDancing = canDoDancing,
        canDoHosting = canDoHosting,
        otherCapabilities = otherCapabilities
    )

    companion object {
        fun fromDomain(capabilities: TalentCapabilities): UpdateCapabilitiesRequest =
            UpdateCapabilitiesRequest(
                canDoVoiceover = capabilities.canDoVoiceover,
                canDoActing = capabilities.canDoActing,
                canDoModeling = capabilities.canDoModeling,
                canDoSinging = capabilities.canDoSinging,
                canDoDancing = capabilities.canDoDancing,
                canDoHosting = capabilities.canDoHosting,
                otherCapabilities = capabilities.otherCapabilities
            )
    }
}

data class UpdateSocialLinksRequest(
    val socialLinks: Set<SocialLinkDto>?
)

data class SocialLinkDto(
    @field:NotBlank(message = "Platform is required")
    val platform: String?,
    @field:NotBlank(message = "URL is required")
    val url: String?,
    val followerCount: Long? = null
) {
    fun toDomain(): SocialLink {
        val platformValue = requireNotNull(platform) { "Platform is required" }
        val urlValue = requireNotNull(url) { "URL is required" }
        return SocialLink(
            platform = platformValue,
            url = urlValue,
            followerCount = followerCount
        )
    }

    companion object {
        fun fromDomain(socialLink: SocialLink): SocialLinkDto = SocialLinkDto(
            platform = socialLink.platform,
            url = socialLink.url,
            followerCount = socialLink.followerCount
        )
    }
}

data class TalentProfileResponse(
    val talentProfileId: UUID,
    val userId: UUID,
    val fullName: String,
    val bio: String?,
    val location: LocationDto,
    val languages: Set<String>,
    val tags: Set<String>,
    val capabilities: UpdateCapabilitiesRequest,
    val socialLinks: Set<SocialLinkDto>,
    val verificationStatus: VerificationStatus,
    val profileImageUrl: String?,
    val createdAt: Instant,
    val updatedAt: Instant,
    // Rating and statistics
    val tier: TalentTier? = null,
    val averageRating: Double? = null,
    val totalRatings: Int? = null,
    val completedCollaborations: Int? = null,
    val successRate: Double? = null
) {
    companion object {
        fun fromDomain(talentProfile: TalentProfile): TalentProfileResponse = TalentProfileResponse(
            talentProfileId = talentProfile.talentProfileId,
            userId = talentProfile.userId,
            fullName = talentProfile.fullName,
            bio = talentProfile.bio,
            location = LocationDto.fromDomain(talentProfile.location),
            languages = talentProfile.languages,
            tags = talentProfile.tags,
            capabilities = UpdateCapabilitiesRequest.fromDomain(talentProfile.capabilities),
            socialLinks = talentProfile.socialLinks.map { SocialLinkDto.fromDomain(it) }.toSet(),
            verificationStatus = talentProfile.verificationStatus,
            profileImageUrl = talentProfile.profileImageUrl,
            createdAt = talentProfile.createdAt,
            updatedAt = talentProfile.updatedAt
        )
    }
}

data class TalentSearchRequest(
    val tags: Set<String>? = null,
    val languages: Set<String>? = null,
    val canDoVoiceover: Boolean? = null,
    val canDoActing: Boolean? = null,
    val canDoModeling: Boolean? = null,
    val canDoSinging: Boolean? = null,
    val canDoDancing: Boolean? = null,
    val canDoHosting: Boolean? = null,
    val latitude: BigDecimal? = null,
    val longitude: BigDecimal? = null,
    val radiusKm: Double? = null,
    val verificationStatus: VerificationStatus? = null,
    // Rating and tier filters
    val minAverageRating: Double? = null,
    val tiers: Set<TalentTier>? = null,
    val minCompletedCollaborations: Int? = null,
    val page: Int = 0,
    val size: Int = 12
)

data class PagedResult<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)
