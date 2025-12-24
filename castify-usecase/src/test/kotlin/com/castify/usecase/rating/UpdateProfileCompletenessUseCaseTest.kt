package com.castify.usecase.rating

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.rating.TalentStatistics
import com.castify.domain.rating.TalentStatisticsRepository
import com.castify.domain.rating.TalentTier
import com.castify.domain.talent.*
import com.castify.usecase.TestHelpers
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.*

class UpdateProfileCompletenessUseCaseTest {

    private lateinit var talentRepository: TalentRepository
    private lateinit var statisticsRepository: TalentStatisticsRepository
    private lateinit var updateProfileCompletenessUseCase: UpdateProfileCompletenessUseCase

    @BeforeEach
    fun setUp() {
        talentRepository = mockk()
        statisticsRepository = mockk()
        updateProfileCompletenessUseCase = UpdateProfileCompletenessUseCase(statisticsRepository, talentRepository)
    }

    @Test
    fun `should successfully update profile completeness`() {
        // Given
        val talentProfileId = UUID.randomUUID()

        val profile = TestHelpers.createValidTalentProfile(
            talentProfileId = talentProfileId,
            userId = UUID.randomUUID(),
            fullName = "John Actor",
            bio = "Experienced actor",
            locationMode = LocationMode.REMOTE,
            primaryLocation = null,
            geoAreas = emptySet(),
            languages = setOf("English", "Spanish"),
            tags = setOf("Actor", "Comedian", "Voice"),
            capabilities = com.castify.domain.talent.TalentCapabilities(
                canDoVoiceover = false,
                canDoActing = true,
                canDoModeling = false,
                canDoSinging = false,
                canDoDancing = false,
                canDoHosting = false
            )
        )

        val stats = TalentStatistics.createInitial(talentProfileId, 0)

        every { talentRepository.findById(talentProfileId) } returns profile
        every { statisticsRepository.findByTalentProfileId(talentProfileId) } returns stats
        every { statisticsRepository.save(any()) } answers { firstArg() }

        // When
        updateProfileCompletenessUseCase.execute(talentProfileId)

        // Then
        verify(exactly = 1) { statisticsRepository.save(any()) }
    }

    @Test
    fun `should throw ResourceNotFoundException when profile not found`() {
        // Given
        val talentProfileId = UUID.randomUUID()

        every { talentRepository.findById(talentProfileId) } returns null

        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            updateProfileCompletenessUseCase.execute(talentProfileId)
        }

        assertTrue(exception.message!!.contains("Talent profile not found"))
        verify(exactly = 0) { statisticsRepository.save(any()) }
    }
}
