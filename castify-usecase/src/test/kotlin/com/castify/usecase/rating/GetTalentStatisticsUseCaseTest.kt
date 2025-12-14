package com.castify.usecase.rating

import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.rating.TalentStatistics
import com.castify.domain.rating.TalentStatisticsRepository
import com.castify.domain.rating.TalentTier
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
import java.time.Instant
import java.util.*

class GetTalentStatisticsUseCaseTest {

    private lateinit var statisticsRepository: TalentStatisticsRepository
    private lateinit var talentRepository: TalentRepository
    private lateinit var getTalentStatisticsUseCase: GetTalentStatisticsUseCase

    @BeforeEach
    fun setUp() {
        statisticsRepository = mockk()
        talentRepository = mockk()
        getTalentStatisticsUseCase = GetTalentStatisticsUseCase(statisticsRepository, talentRepository)
    }

    @Test
    fun `should successfully get talent statistics`() {
        // Given
        val talentProfileId = UUID.randomUUID()

        val talentProfile = TalentProfile(
            talentProfileId = talentProfileId,
            userId = UUID.randomUUID(),
            fullName = "John Actor",
            bio = "Bio",
            location = Location(mode = LocationMode.REMOTE),
            profileImageUrl = null
        )

        val statistics = TalentStatistics(
            talentProfileId = talentProfileId,
            averageRating = 4.5,
            totalRatings = 10,
            averageProfessionalismRating = 4.6,
            averageQualityRating = 4.7,
            averageCommunicationRating = 4.4,
            averageTimelinessRating = 4.3,
            totalCollaborations = 15,
            completedCollaborations = 12,
            canceledCollaborations = 3,
            successRate = 80.0,
            totalEarnings = 5000.0,
            averageResponseTimeHours = 2.5,
            profileCompleteness = 85,
            wouldWorkAgainPercentage = 90.0,
            tier = TalentTier.GOLD,
            lastUpdated = Instant.now()
        )

        every { talentRepository.findById(talentProfileId) } returns talentProfile
        every { statisticsRepository.findByTalentProfileId(talentProfileId) } returns statistics

        // When
        val result = getTalentStatisticsUseCase.execute(talentProfileId)

        // Then
        assertNotNull(result)
        assertEquals(4.5, result.averageRating)
        assertEquals(10, result.totalRatings)
        assertEquals(TalentTier.GOLD, result.tier)

        verify(exactly = 1) { statisticsRepository.findByTalentProfileId(talentProfileId) }
    }

    @Test
    fun `should throw ResourceNotFoundException when talent not found`() {
        // Given
        val talentProfileId = UUID.randomUUID()

        every { talentRepository.findById(talentProfileId) } returns null

        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            getTalentStatisticsUseCase.execute(talentProfileId)
        }

        assertTrue(exception.message!!.contains("Talent profile not found"))
        verify(exactly = 1) { talentRepository.findById(talentProfileId) }
    }
}
