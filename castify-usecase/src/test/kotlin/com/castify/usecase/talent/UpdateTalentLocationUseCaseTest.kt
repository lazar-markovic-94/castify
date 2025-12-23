package com.castify.usecase.talent

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.talent.PrimaryLocation
import com.castify.domain.talent.TalentRepository
import com.castify.usecase.TestHelpers
import com.castify.usecase.dto.PrimaryLocationDto
import com.castify.usecase.dto.UpdatePrimaryLocationRequest
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
    fun `should successfully update primary location`() {
        // Given
        val userId = UUID.randomUUID()
        val existingProfile = TestHelpers.createValidTalentProfile(userId = userId)

        val newLocation = PrimaryLocationDto(
            latitude = BigDecimal("34.0522"),
            longitude = BigDecimal("-118.2437"),
            city = "Los Angeles",
            country = "USA"
        )

        val request = UpdatePrimaryLocationRequest(primaryLocation = newLocation)

        every { talentRepository.findByUserId(userId) } returns existingProfile
        every { talentRepository.save(any()) } answers { firstArg() }

        // When
        val result = updateTalentLocationUseCase.execute(userId, request)

        // Then
        assertNotNull(result)
        assertEquals("Los Angeles", result.primaryLocation?.city)
        assertEquals("USA", result.primaryLocation?.country)

        verify(exactly = 1) { talentRepository.findByUserId(userId) }
        verify(exactly = 1) { talentRepository.save(any()) }
    }

    @Test
    fun `should throw ResourceNotFoundException when profile not found`() {
        // Given
        val userId = UUID.randomUUID()
        val request = UpdatePrimaryLocationRequest(
            primaryLocation = PrimaryLocationDto(
                latitude = BigDecimal("34.0522"),
                longitude = BigDecimal("-118.2437"),
                city = "Los Angeles",
                country = "USA"
            )
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
    fun `should throw IllegalArgumentException when primary location is null`() {
        // Given
        val userId = UUID.randomUUID()
        val existingProfile = TestHelpers.createValidTalentProfile(userId = userId)

        val request = UpdatePrimaryLocationRequest(primaryLocation = null)

        every { talentRepository.findByUserId(userId) } returns existingProfile

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            updateTalentLocationUseCase.execute(userId, request)
        }

        assertEquals("Primary location is required", exception.message)
        verify(exactly = 0) { talentRepository.save(any()) }
    }
}
