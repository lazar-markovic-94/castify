package com.castify.usecase.rating

import com.castify.domain.exception.BadRequestException
import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.rating.TalentRating
import com.castify.domain.rating.TalentRatingRepository
import com.castify.domain.rating.TalentStatistics
import com.castify.domain.rating.TalentStatisticsRepository
import com.castify.domain.rating.TalentTier
import com.castify.domain.talent.LocationMode
import com.castify.domain.talent.TalentProfile
import com.castify.domain.talent.TalentRepository
import com.castify.usecase.TestHelpers
import com.castify.usecase.dto.CreateRatingRequest
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.*

class CreateRatingUseCaseTest {

    private lateinit var ratingRepository: TalentRatingRepository
    private lateinit var statisticsRepository: TalentStatisticsRepository
    private lateinit var talentRepository: TalentRepository
    private lateinit var createRatingUseCase: CreateRatingUseCase

    @BeforeEach
    fun setUp() {
        ratingRepository = mockk()
        statisticsRepository = mockk()
        talentRepository = mockk()
        createRatingUseCase = CreateRatingUseCase(ratingRepository, statisticsRepository, talentRepository)
    }

    @Test
    fun `should successfully create rating and update statistics`() {
        // Given
        val clientUserId = UUID.randomUUID()
        val talentProfileId = UUID.randomUUID()

        val talentProfile = TestHelpers.createValidTalentProfile(
            talentProfileId = talentProfileId,
            userId = UUID.randomUUID(),
            fullName = "John Actor",
            bio = "Bio",
            locationMode = LocationMode.REMOTE,
            primaryLocation = null,
            geoAreas = emptySet()
        )

        val statistics = TalentStatistics.createInitial(talentProfileId, 50)

        val request = CreateRatingRequest(
            talentProfileId = talentProfileId,
            collaborationId = UUID.randomUUID(),
            overallRating = 5,
            professionalismRating = 5,
            qualityRating = 5,
            communicationRating = 5,
            timelinessRating = 5,
            reviewText = "Excellent work!",
            wouldWorkAgain = true
        )

        every { talentRepository.existsById(talentProfileId) } returns true
        every { ratingRepository.findByTalentProfileIdAndClientUserIdAndCollaborationId(talentProfileId, clientUserId, request.collaborationId!!) } returns null
        every { statisticsRepository.findByTalentProfileId(talentProfileId) } returns statistics
        every { ratingRepository.save(any()) } answers { firstArg() }
        every { statisticsRepository.save(any()) } answers { firstArg() }

        // When
        val result = createRatingUseCase.execute(clientUserId, request)

        // Then
        assertNotNull(result)
        assertEquals(5, result.overallRating)
        assertEquals("Excellent work!", result.reviewText)
        assertTrue(result.wouldWorkAgain)

        verify(exactly = 1) { ratingRepository.save(any()) }
        verify(exactly = 1) { statisticsRepository.save(any()) }
    }

    @Test
    fun `should throw ResourceNotFoundException when talent profile not found`() {
        // Given
        val clientUserId = UUID.randomUUID()
        val talentProfileId = UUID.randomUUID()

        val request = CreateRatingRequest(
            talentProfileId = talentProfileId,
            collaborationId = null,
            overallRating = 4,
            professionalismRating = 4,
            qualityRating = 4,
            communicationRating = 4,
            timelinessRating = 4,
            reviewText = null,
            wouldWorkAgain = true
        )

        every { talentRepository.existsById(talentProfileId) } returns false

        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            createRatingUseCase.execute(clientUserId, request)
        }

        assertTrue(exception.message!!.contains("Talent profile not found"))
        verify(exactly = 0) { ratingRepository.save(any()) }
    }

    @Test
    fun `should throw BadRequestException when rating is out of range`() {
        // Given
        val clientUserId = UUID.randomUUID()
        val talentProfileId = UUID.randomUUID()

        val mockTalentProfile: TalentProfile = mockk()

        val request = CreateRatingRequest(
            talentProfileId = talentProfileId,
            collaborationId = null,
            overallRating = 6, // Invalid!
            professionalismRating = 4,
            qualityRating = 4,
            communicationRating = 4,
            timelinessRating = 4,
            reviewText = null,
            wouldWorkAgain = true
        )

        every { talentRepository.existsById(talentProfileId) } returns true

        // When & Then
        assertThrows<BadRequestException> {
            createRatingUseCase.execute(clientUserId, request)
        }
    }

    @Test
    fun `should create initial statistics if none exist`() {
        // Given
        val clientUserId = UUID.randomUUID()
        val talentProfileId = UUID.randomUUID()

        val talentProfile = TestHelpers.createValidTalentProfile(
            talentProfileId = talentProfileId,
            userId = UUID.randomUUID(),
            fullName = "John Actor",
            bio = "Bio",
            locationMode = LocationMode.REMOTE,
            primaryLocation = null,
            geoAreas = emptySet()
        )

        val request = CreateRatingRequest(
            talentProfileId = talentProfileId,
            collaborationId = null,
            overallRating = 4,
            professionalismRating = 4,
            qualityRating = 4,
            communicationRating = 4,
            timelinessRating = 4,
            reviewText = "Good work",
            wouldWorkAgain = true
        )

        every { talentRepository.existsById(talentProfileId) } returns true
        every { statisticsRepository.findByTalentProfileId(talentProfileId) } returns null
        every { ratingRepository.save(any()) } answers { firstArg() }
        every { statisticsRepository.save(any()) } answers { firstArg() }

        // When
        val result = createRatingUseCase.execute(clientUserId, request)

        // Then
        assertNotNull(result)
        verify(exactly = 1) { statisticsRepository.save(any()) }
    }

    @Test
    fun `should handle rating without review text`() {
        // Given
        val clientUserId = UUID.randomUUID()
        val talentProfileId = UUID.randomUUID()

        val talentProfile = TestHelpers.createValidTalentProfile(
            talentProfileId = talentProfileId,
            userId = UUID.randomUUID(),
            fullName = "John Actor",
            bio = "Bio",
            locationMode = LocationMode.REMOTE,
            primaryLocation = null,
            geoAreas = emptySet()
        )

        val statistics = TalentStatistics.createInitial(talentProfileId, 50)

        val request = CreateRatingRequest(
            talentProfileId = talentProfileId,
            collaborationId = null,
            overallRating = 3,
            professionalismRating = 3,
            qualityRating = 3,
            communicationRating = 3,
            timelinessRating = 3,
            reviewText = null,
            wouldWorkAgain = false
        )

        every { talentRepository.existsById(talentProfileId) } returns true
        every { statisticsRepository.findByTalentProfileId(talentProfileId) } returns statistics
        every { ratingRepository.save(any()) } answers { firstArg() }
        every { statisticsRepository.save(any()) } answers { firstArg() }

        // When
        val result = createRatingUseCase.execute(clientUserId, request)

        // Then
        assertNotNull(result)
        assertNull(result.reviewText)
        assertFalse(result.wouldWorkAgain)
    }
}
