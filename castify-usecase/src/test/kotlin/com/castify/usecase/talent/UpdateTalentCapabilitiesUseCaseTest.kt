package com.castify.usecase.talent

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.talent.Location
import com.castify.domain.talent.LocationMode
import com.castify.domain.talent.TalentCapabilities
import com.castify.domain.talent.TalentProfile
import com.castify.domain.talent.TalentRepository
import com.castify.usecase.dto.UpdateCapabilitiesRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class UpdateTalentCapabilitiesUseCaseTest {

    private lateinit var talentRepository: TalentRepository
    private lateinit var updateTalentCapabilitiesUseCase: UpdateTalentCapabilitiesUseCase

    @BeforeEach
    fun setUp() {
        talentRepository = mockk()
        updateTalentCapabilitiesUseCase = UpdateTalentCapabilitiesUseCase(talentRepository)
    }

    @Test
    fun `should successfully update capabilities`() {
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

        val request = UpdateCapabilitiesRequest(
            canDoVoiceover = true,
            canDoActing = true,
            canDoModeling = false,
            canDoSinging = true,
            canDoDancing = false,
            canDoHosting = true
        )

        every { talentRepository.findByUserId(userId) } returns existingProfile
        every { talentRepository.save(any()) } answers { firstArg() }

        // When
        val result = updateTalentCapabilitiesUseCase.execute(userId, request)

        // Then
        assertNotNull(result)
        assertTrue(result.capabilities.canDoVoiceover)
        assertTrue(result.capabilities.canDoActing)
        assertFalse(result.capabilities.canDoModeling)
        assertTrue(result.capabilities.canDoSinging)
        assertFalse(result.capabilities.canDoDancing)
        assertTrue(result.capabilities.canDoHosting)

        verify(exactly = 1) { talentRepository.findByUserId(userId) }
        verify(exactly = 1) { talentRepository.save(any()) }
    }

    @Test
    fun `should successfully update all capabilities to false`() {
        // Given
        val userId = UUID.randomUUID()
        val talentProfileId = UUID.randomUUID()

        val existingProfile = TalentProfile(
            talentProfileId = talentProfileId,
            userId = userId,
            fullName = "John Actor",
            bio = "Bio",
            location = Location(mode = LocationMode.REMOTE),
            profileImageUrl = null,
            capabilities = TalentCapabilities(
                canDoVoiceover = true,
                canDoActing = true,
                canDoModeling = true
            )
        )

        val request = UpdateCapabilitiesRequest(
            canDoVoiceover = false,
            canDoActing = false,
            canDoModeling = false,
            canDoSinging = false,
            canDoDancing = false,
            canDoHosting = false
        )

        every { talentRepository.findByUserId(userId) } returns existingProfile
        every { talentRepository.save(any()) } answers { firstArg() }

        // When
        val result = updateTalentCapabilitiesUseCase.execute(userId, request)

        // Then
        assertNotNull(result)
        assertFalse(result.capabilities.canDoVoiceover)
        assertFalse(result.capabilities.canDoActing)
        assertFalse(result.capabilities.canDoModeling)

        verify(exactly = 1) { talentRepository.save(any()) }
    }

    @Test
    fun `should throw ResourceNotFoundException when profile not found`() {
        // Given
        val userId = UUID.randomUUID()
        val request = UpdateCapabilitiesRequest(
            canDoVoiceover = true,
            canDoActing = false,
            canDoModeling = false,
            canDoSinging = false,
            canDoDancing = false,
            canDoHosting = false
        )

        every { talentRepository.findByUserId(userId) } returns null

        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            updateTalentCapabilitiesUseCase.execute(userId, request)
        }

        assertTrue(exception.message!!.contains("Talent profile not found"))
        verify(exactly = 1) { talentRepository.findByUserId(userId) }
        verify(exactly = 0) { talentRepository.save(any()) }
    }

    @Test
    fun `should update with partial capabilities set to true`() {
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

        val request = UpdateCapabilitiesRequest(
            canDoVoiceover = true,
            canDoActing = false,
            canDoModeling = false,
            canDoSinging = false,
            canDoDancing = false,
            canDoHosting = false
        )

        every { talentRepository.findByUserId(userId) } returns existingProfile
        every { talentRepository.save(any()) } answers { firstArg() }

        // When
        val result = updateTalentCapabilitiesUseCase.execute(userId, request)

        // Then
        assertNotNull(result)
        assertTrue(result.capabilities.canDoVoiceover)
        assertFalse(result.capabilities.canDoActing)
        verify(exactly = 1) { talentRepository.save(any()) }
    }
}
