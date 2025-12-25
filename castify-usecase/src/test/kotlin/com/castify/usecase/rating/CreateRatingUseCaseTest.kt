package com.castify.usecase.rating

import com.castify.domain.exception.BadRequestException
import com.castify.domain.exception.ResourceNotFoundException
import com.castify.domain.rating.TalentRating
import com.castify.domain.rating.TalentRatingRepository
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
    private lateinit var talentRepository: TalentRepository
    private lateinit var statisticsService: TalentStatisticsService
    private lateinit var createRatingUseCase: CreateRatingUseCase

    @BeforeEach
    fun setUp() {
        ratingRepository = mockk()
        talentRepository = mockk()
        statisticsService = mockk(relaxed = true)
        createRatingUseCase = CreateRatingUseCase(ratingRepository, talentRepository, statisticsService)
    }

    @Test
    fun `should successfully create rating`() {
        // Given
        val clientUserId = UUID.randomUUID()
        val talentProfileId = UUID.randomUUID()

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
        every { ratingRepository.save(any()) } answers { firstArg() }

        // When
        val result = createRatingUseCase.execute(clientUserId, request)

        // Then
        assertNotNull(result)
        assertEquals(5, result.overallRating)
        assertEquals("Excellent work!", result.reviewText)
        assertTrue(result.wouldWorkAgain)
        assertTrue(result.isVerified) // Verified because it has collaboration ID

        // Verify statistics service was called
        verify(exactly = 1) { ratingRepository.save(any()) }
        verify(exactly = 1) { statisticsService.updateWithNewRating(any()) }
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
    fun `should create unverified rating when no collaboration ID provided`() {
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
            reviewText = "Good work",
            wouldWorkAgain = true
        )

        every { talentRepository.existsById(talentProfileId) } returns true
        every { ratingRepository.save(any()) } answers { firstArg() }

        // When
        val result = createRatingUseCase.execute(clientUserId, request)

        // Then
        assertNotNull(result)
        assertFalse(result.isVerified) // Not verified because no collaboration ID
        verify(exactly = 1) { ratingRepository.save(any()) }
    }

    @Test
    fun `should handle rating without review text`() {
        // Given
        val clientUserId = UUID.randomUUID()
        val talentProfileId = UUID.randomUUID()

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
        every { ratingRepository.save(any()) } answers { firstArg() }

        // When
        val result = createRatingUseCase.execute(clientUserId, request)

        // Then
        assertNotNull(result)
        assertNull(result.reviewText)
        assertFalse(result.wouldWorkAgain)
        verify(exactly = 1) { ratingRepository.save(any()) }
    }
}
