package com.castify.usecase

import com.castify.domain.talent.*
import java.math.BigDecimal
import java.util.*

/**
 * Test helper functions to create valid test data for talent profiles.
 */
object TestHelpers {

    fun createValidPrimaryLocation(
        city: String = "New York",
        country: String = "USA",
        latitude: BigDecimal = BigDecimal("40.7128"),
        longitude: BigDecimal = BigDecimal("-74.0060")
    ) = PrimaryLocation(
        latitude = latitude,
        longitude = longitude,
        city = city,
        country = country
    )

    fun createValidGeoArea(
        city: String = "New York",
        country: String = "USA",
        isPrimary: Boolean = true
    ) = GeoArea(
        city = city,
        country = country,
        isPrimary = isPrimary
    )

    fun createValidTalentProfile(
        talentProfileId: UUID = UUID.randomUUID(),
        userId: UUID = UUID.randomUUID(),
        fullName: String = "John Doe",
        bio: String? = null,
        locationMode: LocationMode = LocationMode.SPECIFIC,
        primaryLocation: PrimaryLocation? = createValidPrimaryLocation(),
        geoAreas: Set<GeoArea> = setOf(createValidGeoArea()),
        languages: Set<String> = emptySet(),
        tags: Set<String> = emptySet(),
        capabilities: TalentCapabilities = TalentCapabilities(),
        socialLinks: Set<SocialLink> = emptySet(),
        verificationStatus: VerificationStatus = VerificationStatus.PENDING,
        profileImageUrl: String? = null
    ) = TalentProfile(
        talentProfileId = talentProfileId,
        userId = userId,
        fullName = fullName,
        bio = bio,
        locationMode = locationMode,
        primaryLocation = primaryLocation,
        geoAreas = geoAreas,
        languages = languages,
        tags = tags,
        capabilities = capabilities,
        socialLinks = socialLinks,
        verificationStatus = verificationStatus,
        profileImageUrl = profileImageUrl
    )
}
