package com.castify.usecase.rating

import com.castify.domain.rating.TalentStatistics
import com.castify.domain.rating.TalentStatisticsRepository
import com.castify.domain.rating.TalentTier
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

class GetLeaderboardUseCaseTest {

    private lateinit var statisticsRepository: TalentStatisticsRepository
    private lateinit var getLeaderboardUseCase: GetLeaderboardUseCase

    @BeforeEach
    fun setUp() {
        statisticsRepository = mockk()
        getLeaderboardUseCase = GetLeaderboardUseCase(statisticsRepository)
    }

    @Test
    fun `should successfully get leaderboard with default limit`() {
        // Given
        val topRatedStats = listOf(
            TalentStatistics(
                talentProfileId = UUID.randomUUID(),
                averageRating = 4.9,
                totalRatings = 50,
                averageProfessionalismRating = 4.9,
                averageQualityRating = 4.9,
                averageCommunicationRating = 4.9,
                averageTimelinessRating = 4.9,
                totalCollaborations = 60,
                completedCollaborations = 55,
                canceledCollaborations = 5,
                successRate = 91.67,
                totalEarnings = 50000.0,
                averageResponseTimeHours = 1.0,
                profileCompleteness = 95,
                wouldWorkAgainPercentage = 95.0,
                tier = TalentTier.PREMIUM,
                lastUpdated = Instant.now()
            )
        )

        val mostSuccessfulStats = listOf(
            TalentStatistics(
                talentProfileId = UUID.randomUUID(),
                averageRating = 4.7,
                totalRatings = 100,
                averageProfessionalismRating = 4.7,
                averageQualityRating = 4.7,
                averageCommunicationRating = 4.7,
                averageTimelinessRating = 4.7,
                totalCollaborations = 120,
                completedCollaborations = 110,
                canceledCollaborations = 10,
                successRate = 91.67,
                totalEarnings = 100000.0,
                averageResponseTimeHours = 2.0,
                profileCompleteness = 90,
                wouldWorkAgainPercentage = 92.0,
                tier = TalentTier.PREMIUM,
                lastUpdated = Instant.now()
            )
        )

        every { statisticsRepository.findTopRated(10) } returns topRatedStats
        every { statisticsRepository.findMostSuccessful(10) } returns mostSuccessfulStats

        // When
        val result = getLeaderboardUseCase.execute()

        // Then
        assertNotNull(result)
        assertEquals(1, result.topRated.size)
        assertEquals(1, result.mostSuccessful.size)
        assertEquals(4.9, result.topRated[0].averageRating)
        assertEquals(110, result.mostSuccessful[0].completedCollaborations)

        verify(exactly = 1) { statisticsRepository.findTopRated(10) }
        verify(exactly = 1) { statisticsRepository.findMostSuccessful(10) }
    }

    @Test
    fun `should successfully get leaderboard with custom limit`() {
        // Given
        val limit = 5
        val topRatedStats = emptyList<TalentStatistics>()
        val mostSuccessfulStats = emptyList<TalentStatistics>()

        every { statisticsRepository.findTopRated(limit) } returns topRatedStats
        every { statisticsRepository.findMostSuccessful(limit) } returns mostSuccessfulStats

        // When
        val result = getLeaderboardUseCase.execute(limit)

        // Then
        assertNotNull(result)
        assertTrue(result.topRated.isEmpty())
        assertTrue(result.mostSuccessful.isEmpty())

        verify(exactly = 1) { statisticsRepository.findTopRated(limit) }
        verify(exactly = 1) { statisticsRepository.findMostSuccessful(limit) }
    }

    @Test
    fun `should handle multiple entries in leaderboard`() {
        // Given
        val topRatedStats = (1..3).map {
            TalentStatistics(
                talentProfileId = UUID.randomUUID(),
                averageRating = 4.5 + (it * 0.1),
                totalRatings = 10 * it,
                averageProfessionalismRating = 4.5,
                averageQualityRating = 4.5,
                averageCommunicationRating = 4.5,
                averageTimelinessRating = 4.5,
                totalCollaborations = 20 * it,
                completedCollaborations = 18 * it,
                canceledCollaborations = 2 * it,
                successRate = 90.0,
                totalEarnings = 10000.0 * it,
                averageResponseTimeHours = 2.0,
                profileCompleteness = 80,
                wouldWorkAgainPercentage = 85.0,
                tier = TalentTier.GOLD,
                lastUpdated = Instant.now()
            )
        }

        val mostSuccessfulStats = (1..3).map {
            TalentStatistics(
                talentProfileId = UUID.randomUUID(),
                averageRating = 4.0,
                totalRatings = 5 * it,
                averageProfessionalismRating = 4.0,
                averageQualityRating = 4.0,
                averageCommunicationRating = 4.0,
                averageTimelinessRating = 4.0,
                totalCollaborations = 50 * it,
                completedCollaborations = 45 * it,
                canceledCollaborations = 5 * it,
                successRate = 90.0,
                totalEarnings = 50000.0 * it,
                averageResponseTimeHours = 3.0,
                profileCompleteness = 75,
                wouldWorkAgainPercentage = 80.0,
                tier = TalentTier.SILVER,
                lastUpdated = Instant.now()
            )
        }

        every { statisticsRepository.findTopRated(10) } returns topRatedStats
        every { statisticsRepository.findMostSuccessful(10) } returns mostSuccessfulStats

        // When
        val result = getLeaderboardUseCase.execute()

        // Then
        assertNotNull(result)
        assertEquals(3, result.topRated.size)
        assertEquals(3, result.mostSuccessful.size)

        // Verify all entries have required fields
        result.topRated.forEach { entry ->
            assertNotNull(entry.talentProfileId)
            assertTrue(entry.averageRating > 0)
            assertTrue(entry.completedCollaborations >= 0)
            assertNotNull(entry.tier)
        }

        result.mostSuccessful.forEach { entry ->
            assertNotNull(entry.talentProfileId)
            assertTrue(entry.averageRating > 0)
            assertTrue(entry.completedCollaborations >= 0)
            assertNotNull(entry.tier)
        }
    }
}
