package com.castify.usecase.talent

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.talent.Location
import com.castify.domain.talent.LocationMode
import com.castify.domain.talent.TalentProfile
import com.castify.domain.talent.TalentRepository
import com.castify.usecase.dto.UpdateTagsRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class UpdateTalentTagsUseCaseTest {

    private lateinit var talentRepository: TalentRepository
    private lateinit var updateTalentTagsUseCase: UpdateTalentTagsUseCase

    @BeforeEach
    fun setUp() {
        talentRepository = mockk()
        updateTalentTagsUseCase = UpdateTalentTagsUseCase(talentRepository)
    }

    @Test
    fun `should successfully update tags`() {
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

        val tags = setOf("Actor", "Comedian", "Voice Artist")
        val request = UpdateTagsRequest(tags = tags)

        every { talentRepository.findByUserId(userId) } returns existingProfile
        every { talentRepository.save(any()) } answers { firstArg() }

        // When
        val result = updateTalentTagsUseCase.execute(userId, request)

        // Then
        assertNotNull(result)
        assertEquals(3, result.tags.size)
        assertTrue(result.tags.containsAll(tags))

        verify(exactly = 1) { talentRepository.findByUserId(userId) }
        verify(exactly = 1) { talentRepository.save(any()) }
    }

    @Test
    fun `should successfully clear all tags`() {
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
            tags = setOf("Old Tag", "Another Tag")
        )

        val request = UpdateTagsRequest(tags = emptySet())

        every { talentRepository.findByUserId(userId) } returns existingProfile
        every { talentRepository.save(any()) } answers { firstArg() }

        // When
        val result = updateTalentTagsUseCase.execute(userId, request)

        // Then
        assertNotNull(result)
        assertTrue(result.tags.isEmpty())

        verify(exactly = 1) { talentRepository.save(any()) }
    }

    @Test
    fun `should throw ResourceNotFoundException when profile not found`() {
        // Given
        val userId = UUID.randomUUID()
        val request = UpdateTagsRequest(tags = setOf("Actor"))

        every { talentRepository.findByUserId(userId) } returns null

        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            updateTalentTagsUseCase.execute(userId, request)
        }

        assertTrue(exception.message!!.contains("Talent profile not found"))
        verify(exactly = 1) { talentRepository.findByUserId(userId) }
        verify(exactly = 0) { talentRepository.save(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when tags is null`() {
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

        val request = UpdateTagsRequest(tags = null)

        every { talentRepository.findByUserId(userId) } returns existingProfile

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            updateTalentTagsUseCase.execute(userId, request)
        }

        assertEquals("Tags list is required", exception.message)
        verify(exactly = 0) { talentRepository.save(any()) }
    }
}
