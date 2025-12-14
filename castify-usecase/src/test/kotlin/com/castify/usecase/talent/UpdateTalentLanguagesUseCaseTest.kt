package com.castify.usecase.talent

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.talent.Location
import com.castify.domain.talent.LocationMode
import com.castify.domain.talent.TalentProfile
import com.castify.domain.talent.TalentRepository
import com.castify.usecase.dto.UpdateLanguagesRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

class UpdateTalentLanguagesUseCaseTest {

    private lateinit var talentRepository: TalentRepository
    private lateinit var updateTalentLanguagesUseCase: UpdateTalentLanguagesUseCase

    @BeforeEach
    fun setUp() {
        talentRepository = mockk()
        updateTalentLanguagesUseCase = UpdateTalentLanguagesUseCase(talentRepository)
    }

    @Test
    fun `should successfully update languages`() {
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

        val languages = setOf("English", "Spanish", "French")
        val request = UpdateLanguagesRequest(languages = languages)

        every { talentRepository.findByUserId(userId) } returns existingProfile
        every { talentRepository.save(any()) } answers { firstArg() }

        // When
        val result = updateTalentLanguagesUseCase.execute(userId, request)

        // Then
        assertNotNull(result)
        assertEquals(3, result.languages.size)
        assertTrue(result.languages.containsAll(languages))

        verify(exactly = 1) { talentRepository.findByUserId(userId) }
        verify(exactly = 1) { talentRepository.save(any()) }
    }

    @Test
    fun `should successfully update to empty languages list`() {
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
            languages = setOf("English", "German")
        )

        val request = UpdateLanguagesRequest(languages = emptySet())

        every { talentRepository.findByUserId(userId) } returns existingProfile
        every { talentRepository.save(any()) } answers { firstArg() }

        // When
        val result = updateTalentLanguagesUseCase.execute(userId, request)

        // Then
        assertNotNull(result)
        assertTrue(result.languages.isEmpty())

        verify(exactly = 1) { talentRepository.save(any()) }
    }

    @Test
    fun `should throw ResourceNotFoundException when profile not found`() {
        // Given
        val userId = UUID.randomUUID()
        val request = UpdateLanguagesRequest(languages = setOf("English"))

        every { talentRepository.findByUserId(userId) } returns null

        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            updateTalentLanguagesUseCase.execute(userId, request)
        }

        assertTrue(exception.message!!.contains("Talent profile not found"))
        verify(exactly = 1) { talentRepository.findByUserId(userId) }
        verify(exactly = 0) { talentRepository.save(any()) }
    }

    @Test
    fun `should throw IllegalArgumentException when languages is null`() {
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

        val request = UpdateLanguagesRequest(languages = null)

        every { talentRepository.findByUserId(userId) } returns existingProfile

        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            updateTalentLanguagesUseCase.execute(userId, request)
        }

        assertEquals("Languages are required", exception.message)
        verify(exactly = 0) { talentRepository.save(any()) }
    }
}
