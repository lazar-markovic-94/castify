package com.castify.usecase.talent

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.talent.Location
import com.castify.domain.talent.LocationMode
import com.castify.domain.talent.TalentProfile
import com.castify.domain.talent.TalentRepository
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
    private lateinit var getTalentProfileUseCase: GetTalentProfileUseCase

    @BeforeEach
    fun setUp() {
        talentRepository = mockk()
        val profileEnricher = mockk<com.castify.usecase.talent.TalentProfileEnricher>(relaxed = true)
        getTalentProfileUseCase = GetTalentProfileUseCase(talentRepository, profileEnricher)
    }

    @Test
    fun `should successfully get talent profile by ID`() {
        // Given
        val talentProfileId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val location = Location(
            mode = LocationMode.SPECIFIC,
            latitude = BigDecimal("34.0522"),
            longitude = BigDecimal("-118.2437"),
            city = "Los Angeles",
            country = "USA"
        )

        val profile = TalentProfile(
            talentProfileId = talentProfileId,
            userId = userId,
            fullName = "John Actor",
            bio = "Experienced actor with 10 years",
            location = location,
            profileImageUrl = "https://example.com/profile.jpg"
        )

        every { talentRepository.findById(talentProfileId) } returns profile

        // When
        val result = getTalentProfileUseCase.execute(talentProfileId)

        // Then
        assertNotNull(result)
        assertEquals("John Actor", result.fullName)
        assertEquals("Experienced actor with 10 years", result.bio)
        assertEquals(LocationMode.SPECIFIC, result.location.mode)
        assertEquals("Los Angeles", result.location.city)

        verify(exactly = 1) { talentRepository.findById(talentProfileId) }
    }

    @Test
    fun `should successfully get talent profile by user ID`() {
        // Given
        val talentProfileId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val location = Location(
            mode = LocationMode.REMOTE,
            latitude = null,
            longitude = null,
            city = null,
            country = null
        )

        val profile = TalentProfile(
            talentProfileId = talentProfileId,
            userId = userId,
            fullName = "Remote Worker",
            bio = "Works remotely",
            location = location,
            profileImageUrl = null
        )

        every { talentRepository.findByUserId(userId) } returns profile

        // When
        val result = getTalentProfileUseCase.executeByUserId(userId)

        // Then
        assertNotNull(result)
        assertEquals("Remote Worker", result.fullName)
        assertEquals(LocationMode.REMOTE, result.location.mode)

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

        assertEquals("Talent profile not found", exception.message)
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

        assertEquals("Talent profile not found for user", exception.message)
        verify(exactly = 1) { talentRepository.findByUserId(userId) }
    }

    @Test
    fun `should successfully get profile with all optional fields populated`() {
        // Given
        val talentProfileId = UUID.randomUUID()
        val userId = UUID.randomUUID()

        val location = Location(
            mode = LocationMode.SPECIFIC,
            latitude = BigDecimal("40.7128"),
            longitude = BigDecimal("-74.0060"),
            city = "New York",
            country = "USA"
        )

        val profile = TalentProfile(
            talentProfileId = talentProfileId,
            userId = userId,
            fullName = "Complete Profile",
            bio = "This is a complete bio with all details",
            location = location,
            profileImageUrl = "https://example.com/full-profile.jpg"
        )

        every { talentRepository.findById(talentProfileId) } returns profile

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

        val location = Location(
            mode = LocationMode.REMOTE,
            latitude = null,
            longitude = null,
            city = null,
            country = null
        )

        val profile = TalentProfile(
            talentProfileId = talentProfileId,
            userId = userId,
            fullName = "Minimal Profile",
            bio = null,
            location = location,
            profileImageUrl = null
        )

        every { talentRepository.findById(talentProfileId) } returns profile

        // When
        val result = getTalentProfileUseCase.execute(talentProfileId)

        // Then
        assertNotNull(result)
        assertEquals("Minimal Profile", result.fullName)
        assertNull(result.bio)
        assertNull(result.profileImageUrl)
    }
}
