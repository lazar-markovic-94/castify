package com.castify.usecase.talent

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.talent.Location
import com.castify.domain.talent.LocationMode
import com.castify.domain.talent.TalentProfile
import com.castify.domain.talent.TalentRepository
import com.castify.usecase.dto.LocationDto
import com.castify.usecase.dto.UpdateLocationRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.util.*

class UpdateTalentLocationUseCaseTest {

    private lateinit var talentRepository: TalentRepository
    private lateinit var updateTalentLocationUseCase: UpdateTalentLocationUseCase

    @BeforeEach
    fun setUp() {
        talentRepository = mockk()
        updateTalentLocationUseCase = UpdateTalentLocationUseCase(talentRepository)
    }

    @Test
    fun `should successfully update location to specific`() {
        // Given
        val userId = UUID.randomUUID()
        val talentProfileId = UUID.randomUUID()

        val existingProfile = TalentProfile(
            talentProfileId = talentProfileId,
            userId = userId,
            fullName = "John Actor",
            bio = "Bio",
            location = Location(mode = LocationMode.REMOTE),
            profileImageUrl = null
        )

        val newLocation = LocationDto(
            mode = LocationMode.SPECIFIC,
            latitude = BigDecimal("34.0522"),
            longitude = BigDecimal("-118.2437"),
            city = "Los Angeles",
            country = "USA"
        )

        val request = UpdateLocationRequest(location = newLocation)

        every { talentRepository.findByUserId(userId) } returns existingProfile
        every { talentRepository.save(any()) } answers { firstArg() }

        // When
        val result = updateTalentLocationUseCase.execute(userId, request)

        // Then
        assertNotNull(result)
        assertEquals(LocationMode.SPECIFIC, result.location.mode)
        assertEquals("Los Angeles", result.location.city)
        assertEquals("USA", result.location.country)

        verify(exactly = 1) { talentRepository.findByUserId(userId) }
        verify(exactly = 1) { talentRepository.save(any()) }
    }

    @Test
    fun `should successfully update location to remote`() {
        // Given
        val userId = UUID.randomUUID()
        val talentProfileId = UUID.randomUUID()

        val existingProfile = TalentProfile(
            talentProfileId = talentProfileId,
            userId = userId,
            fullName = "John Actor",
            bio = "Bio",
            location = Location(
                mode = LocationMode.SPECIFIC,
                latitude = BigDecimal("40.7128"),
                longitude = BigDecimal("-74.0060"),
                city = "New York",
                country = "USA"
            ),
            profileImageUrl = null
        )

        val newLocation = LocationDto(mode = LocationMode.REMOTE)
        val request = UpdateLocationRequest(location = newLocation)

        every { talentRepository.findByUserId(userId) } returns existingProfile
        every { talentRepository.save(any()) } answers { firstArg() }

        // When
        val result = updateTalentLocationUseCase.execute(userId, request)

        // Then
        assertNotNull(result)
        assertEquals(LocationMode.REMOTE, result.location.mode)

        verify(exactly = 1) { talentRepository.save(any()) }
    }

    @Test
    fun `should throw ResourceNotFoundException when profile not found`() {
        // Given
        val userId = UUID.randomUUID()
        val request = UpdateLocationRequest(
            location = LocationDto(mode = LocationMode.REMOTE)
        )

        every { talentRepository.findByUserId(userId) } returns null

        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            updateTalentLocationUseCase.execute(userId, request)
        }

        assertTrue(exception.message!!.contains("Talent profile not found"))
        verify(exactly = 1) { talentRepository.findByUserId(userId) }
        verify(exactly = 0) { talentRepository.save(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when location is null`() {
        // Given
        val userId = UUID.randomUUID()
        val talentProfileId = UUID.randomUUID()

        val existingProfile = TalentProfile(
            talentProfileId = talentProfileId,
            userId = userId,
            fullName = "John Actor",
            bio = "Bio",
            location = Location(mode = LocationMode.REMOTE),
            profileImageUrl = null
        )

        val request = UpdateLocationRequest(location = null)

        every { talentRepository.findByUserId(userId) } returns existingProfile

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            updateTalentLocationUseCase.execute(userId, request)
        }

        assertEquals("Location is required", exception.message)
        verify(exactly = 0) { talentRepository.save(any()) }
    }
}
