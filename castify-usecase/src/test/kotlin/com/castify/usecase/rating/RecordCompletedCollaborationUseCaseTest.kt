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
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.*

class RecordCompletedCollaborationUseCaseTest {

    private lateinit var statisticsRepository: TalentStatisticsRepository
    private lateinit var talentRepository: TalentRepository
    private lateinit var recordCompletedCollaborationUseCase: RecordCompletedCollaborationUseCase

    @BeforeEach
    fun setUp() {
        statisticsRepository = mockk()
        talentRepository = mockk()
        recordCompletedCollaborationUseCase = RecordCompletedCollaborationUseCase(statisticsRepository, talentRepository)
    }

    @Test
    fun `should successfully record completed collaboration`() {
        // Given
        val talentProfileId = UUID.randomUUID()
        val earnings = 500.0

        val talentProfile = TalentProfile(
            talentProfileId = talentProfileId,
            userId = UUID.randomUUID(),
            fullName = "John Actor",
            bio = "Bio",
            location = Location(mode = LocationMode.REMOTE),
            profileImageUrl = null
        )

        val currentStats = TalentStatistics(
            talentProfileId = talentProfileId,
            averageRating = 4.5,
            totalRatings = 5,
            averageProfessionalismRating = 4.5,
            averageQualityRating = 4.5,
            averageCommunicationRating = 4.5,
            averageTimelinessRating = 4.5,
            totalCollaborations = 10,
            completedCollaborations = 8,
            canceledCollaborations = 2,
            successRate = 80.0,
            totalEarnings = 4000.0,
            averageResponseTimeHours = 2.0,
            profileCompleteness = 80,
            wouldWorkAgainPercentage = 85.0,
            tier = TalentTier.GOLD,
            lastUpdated = Instant.now()
        )

        val savedSlot = slot<TalentStatistics>()
        every { talentRepository.findById(talentProfileId) } returns talentProfile
        every { statisticsRepository.findByTalentProfileId(talentProfileId) } returns currentStats
        every { statisticsRepository.save(capture(savedSlot)) } answers { firstArg() }

        // When
        recordCompletedCollaborationUseCase.execute(talentProfileId, earnings)

        // Then
        verify(exactly = 1) { statisticsRepository.save(any()) }

        val saved = savedSlot.captured
        assertEquals(11, saved.totalCollaborations)
        assertEquals(9, saved.completedCollaborations)
        assertEquals(4500.0, saved.totalEarnings)
        assertTrue(saved.successRate > 80.0)
    }

    @Test
    fun `should throw ResourceNotFoundException when talent not found`() {
        // Given
        val talentProfileId = UUID.randomUUID()

        every { talentRepository.findById(talentProfileId) } returns null

        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            recordCompletedCollaborationUseCase.execute(talentProfileId, 100.0)
        }

        assertTrue(exception.message!!.contains("Talent profile not found"))
        verify(exactly = 0) { statisticsRepository.save(any()) }
    }
}
