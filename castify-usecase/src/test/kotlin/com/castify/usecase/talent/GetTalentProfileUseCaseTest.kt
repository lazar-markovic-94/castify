package com.castify.usecase.talent

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.talent.GeoArea
import com.castify.domain.talent.LocationMode
import com.castify.domain.talent.PrimaryLocation
import com.castify.domain.talent.TalentProfile
import com.castify.domain.talent.TalentRepository
import com.castify.usecase.TestHelpers
import com.castify.usecase.dto.TalentProfileResponse
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.*

class GetTalentProfileUseCaseTest {

    private lateinit var talentRepository: TalentRepository
    private lateinit var profileEnricher: TalentProfileEnricher
    private lateinit var getTalentProfileUseCase: GetTalentProfileUseCase

    @BeforeEach
    fun setUp() {
        talentRepository = mockk()
        profileEnricher = mockk()
        getTalentProfileUseCase = GetTalentProfileUseCase(talentRepository, profileEnricher)
    }

    @Test
    fun `should successfully get talent profile by ID`() {
        // Given
        val talentProfileId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val profile = TestHelpers.createValidTalentProfile(
            talentProfileId = talentProfileId,
            userId = userId,
            fullName = "John Actor",
            bio = "Experienced actor with 10 years",
            locationMode = LocationMode.SPECIFIC,
            primaryLocation = PrimaryLocation(
                latitude = BigDecimal("34.0522"),
                longitude = BigDecimal("-118.2437"),
                city = "Los Angeles",
                country = "USA"
            ),
            geoAreas = setOf(GeoArea("Los Angeles", "USA", true))
        )

        every { talentRepository.findById(talentProfileId) } returns profile
        every { profileEnricher.enrichProfile(profile) } returns TalentProfileResponse.fromDomain(profile)

        // When
        val result = getTalentProfileUseCase.execute(talentProfileId)

        // Then
        assertNotNull(result)
        assertEquals("John Actor", result.fullName)
        assertEquals("Experienced actor with 10 years", result.bio)
        assertEquals(LocationMode.SPECIFIC, result.locationMode)
        assertEquals("Los Angeles", result.primaryLocation?.city)

        verify(exactly = 1) { talentRepository.findById(talentProfileId) }
    }

    @Test
    fun `should successfully get talent profile by user ID`() {
        // Given
        val talentProfileId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val profile = TestHelpers.createValidTalentProfile(
            talentProfileId = talentProfileId,
            userId = userId,
            fullName = "Remote Worker",
            bio = "Works remotely",
            locationMode = LocationMode.REMOTE,
            primaryLocation = null,
            geoAreas = emptySet()
        )

        every { talentRepository.findByUserId(userId) } returns profile
        every { profileEnricher.enrichProfile(profile) } returns TalentProfileResponse.fromDomain(profile)

        // When
        val result = getTalentProfileUseCase.executeByUserId(userId)

        // Then
        assertNotNull(result)
        assertEquals("Remote Worker", result.fullName)
        assertEquals(LocationMode.REMOTE, result.locationMode)

        verify(exactly = 1) { talentRepository.findByUserId(userId) }
    }

    @Test
    fun `should throw ResourceNotFoundException when profile not found by ID`() {
        // Given
        val talentProfileId = UUID.randomUUID()

        every { talentRepository.findById(talentProfileId) } returns null

        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            getTalentProfileUseCase.execute(talentProfileId)
        }

        assertTrue(exception.message?.contains("Talent profile not found") ?: false)
        verify(exactly = 1) { talentRepository.findById(talentProfileId) }
    }

    @Test
    fun `should throw ResourceNotFoundException when profile not found by user ID`() {
        // Given
        val userId = UUID.randomUUID()

        every { talentRepository.findByUserId(userId) } returns null

        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            getTalentProfileUseCase.executeByUserId(userId)
        }

        assertTrue(exception.message?.contains("Talent profile not found for user") ?: false)
        verify(exactly = 1) { talentRepository.findByUserId(userId) }
    }

    @Test
    fun `should successfully get profile with all optional fields populated`() {
        // Given
        val talentProfileId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val profile = TestHelpers.createValidTalentProfile(
            talentProfileId = talentProfileId,
            userId = userId,
            fullName = "Complete Profile",
            bio = "This is a complete bio with all details",
            locationMode = LocationMode.SPECIFIC,
            primaryLocation = PrimaryLocation(
                latitude = BigDecimal("40.7128"),
                longitude = BigDecimal("-74.0060"),
                city = "New York",
                country = "USA"
            ),
            geoAreas = setOf(GeoArea("New York", "USA", true)),
            profileImageUrl = "https://example.com/full-profile.jpg"
        )

        every { talentRepository.findById(talentProfileId) } returns profile
        every { profileEnricher.enrichProfile(profile) } returns TalentProfileResponse.fromDomain(profile)

        // When
        val result = getTalentProfileUseCase.execute(talentProfileId)

        // Then
        assertNotNull(result)
        assertNotNull(result.bio)
        assertNotNull(result.profileImageUrl)
        assertEquals("https://example.com/full-profile.jpg", result.profileImageUrl)
    }

    @Test
    fun `should successfully get profile with minimal fields`() {
        // Given
        val talentProfileId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val profile = TestHelpers.createValidTalentProfile(
            talentProfileId = talentProfileId,
            userId = userId,
            fullName = "Minimal Profile",
            bio = null,
            locationMode = LocationMode.REMOTE,
            primaryLocation = null,
            geoAreas = emptySet()
        )

        every { talentRepository.findById(talentProfileId) } returns profile
        every { profileEnricher.enrichProfile(profile) } returns TalentProfileResponse.fromDomain(profile)

        // When
        val result = getTalentProfileUseCase.execute(talentProfileId)

        // Then
        assertNotNull(result)
        assertEquals("Minimal Profile", result.fullName)
        assertNull(result.bio)
        assertNull(result.profileImageUrl)
    }
}
